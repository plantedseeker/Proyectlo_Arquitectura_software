import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

const baseUrl = __ENV.BASE_URL || 'http://api:8080';
const chatId = __ENV.CHAT_ID || '3fb4ce25-b840-4685-d2ea-53f9a4bdedc6';
const loadLevel = Number(__ENV.LOAD_LEVEL || '10');
const iterationsPerVu = Number(__ENV.ITERATIONS_PER_VU || '4');
const expectedRequests = loadLevel * iterationsPerVu;
const runNumber = Number(__ENV.RUN_NUMBER || '1');
const summaryPath = __ENV.SUMMARY_PATH || `/growth/resultados/run-vu${loadLevel}-${runNumber}.json`;

const measuredRequests = new Counter('measured_requests');
const validMessagePage = new Rate('valid_message_page');
const messagePageDuration = new Trend('message_page_duration', true);
const status200 = new Counter('status_200');
const status401 = new Counter('status_401');
const status403 = new Counter('status_403');
const status404 = new Counter('status_404');
const status429 = new Counter('status_429');
const status5xx = new Counter('status_5xx');
const statusOther = new Counter('status_other');

if (![10, 50, 100, 150, 200].includes(loadLevel)) {
  throw new Error(`LOAD_LEVEL no prerregistrado: ${loadLevel}`);
}
if (iterationsPerVu !== 4) {
  throw new Error(`ITERATIONS_PER_VU no prerregistrado: ${iterationsPerVu}`);
}

export const options = {
  scenarios: {
    latest_messages_growth: {
      executor: 'shared-iterations',
      vus: loadLevel,
      iterations: expectedRequests,
      maxDuration: '2m',
    },
  },
  thresholds: {
    checks: ['rate==1'],
    http_req_failed: ['rate==0'],
    measured_requests: [`count==${expectedRequests}`],
    valid_message_page: ['rate==1'],
    message_page_duration: ['p(95)<=500'],
  },
  summaryTrendStats: ['min', 'avg', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

export function setup() {
  const response = http.post(
    `${baseUrl}/api/auth/login`,
    JSON.stringify({
      email: 'estudiante@utrabajo.local',
      password: 'UTrabajo1!',
    }),
    { headers: { 'Content-Type': 'application/json', Accept: 'application/json' } },
  );
  const authenticated = check(response, {
    'login status is 200': (result) => result.status === 200,
    'login returns token': (result) => Boolean(result.json('token')),
  });
  if (!authenticated) {
    throw new Error(`No fue posible autenticar la cuenta de carga: HTTP ${response.status}`);
  }
  return { token: response.json('token') };
}

function countStatus(status) {
  if (status === 200) status200.add(1);
  else if (status === 401) status401.add(1);
  else if (status === 403) status403.add(1);
  else if (status === 404) status404.add(1);
  else if (status === 429) status429.add(1);
  else if (status >= 500 && status <= 599) status5xx.add(1);
  else statusOther.add(1);
}

export default function (data) {
  const response = http.get(
    `${baseUrl}/api/chats/${chatId}/messages?limit=50&offset=0`,
    {
      headers: {
        Authorization: `Bearer ${data.token}`,
        Accept: 'application/json',
      },
      tags: { operation: 'latest_50_messages_growth', load_level: String(loadLevel) },
    },
  );

  countStatus(response.status);
  const valid = check(response, {
    'messages status is 200': (result) => result.status === 200,
    'response contains 50 messages': (result) => {
      try {
        const body = result.json();
        return Array.isArray(body) && body.length === 50;
      } catch (_) {
        return false;
      }
    },
  });
  measuredRequests.add(1);
  validMessagePage.add(valid);
  messagePageDuration.add(response.timings.duration);
}

function thresholdPassed(metric) {
  return Object.values((metric || {}).thresholds || {}).every((threshold) => threshold.ok);
}

function metricValue(data, metric, field, fallback = 0) {
  return data.metrics[metric] && data.metrics[metric].values[field] !== undefined
    ? data.metrics[metric].values[field]
    : fallback;
}

export function handleSummary(data) {
  const duration = data.metrics.message_page_duration.values;
  const result = {
    schema_version: 1,
    run: runNumber,
    discarded: runNumber === 1,
    captured_at_utc: new Date().toISOString(),
    tool: 'k6',
    scenario: 'latest_messages_growth',
    load_level_vus: loadLevel,
    iterations_per_vu: iterationsPerVu,
    expected_requests: expectedRequests,
    requests: metricValue(data, 'measured_requests', 'count'),
    throughput_rps: metricValue(data, 'measured_requests', 'rate'),
    checks_rate: metricValue(data, 'checks', 'rate'),
    failed_request_rate: metricValue(data, 'http_req_failed', 'rate'),
    valid_page_rate: metricValue(data, 'valid_message_page', 'rate'),
    latency_ms: {
      min: duration.min,
      mean: duration.avg,
      p50: duration.med,
      p90: duration['p(90)'],
      p95: duration['p(95)'],
      p99: duration['p(99)'],
      max: duration.max,
    },
    iteration_duration_ms: {
      mean: metricValue(data, 'iteration_duration', 'avg'),
      p95: metricValue(data, 'iteration_duration', 'p(95)'),
      max: metricValue(data, 'iteration_duration', 'max'),
    },
    response_status_counts: {
      200: metricValue(data, 'status_200', 'count'),
      401: metricValue(data, 'status_401', 'count'),
      403: metricValue(data, 'status_403', 'count'),
      404: metricValue(data, 'status_404', 'count'),
      429: metricValue(data, 'status_429', 'count'),
      '5xx': metricValue(data, 'status_5xx', 'count'),
      other: metricValue(data, 'status_other', 'count'),
    },
    integrity_valid: metricValue(data, 'measured_requests', 'count') === expectedRequests,
    thresholds_passed:
      thresholdPassed(data.metrics.checks) &&
      thresholdPassed(data.metrics.http_req_failed) &&
      thresholdPassed(data.metrics.measured_requests) &&
      thresholdPassed(data.metrics.valid_message_page) &&
      thresholdPassed(data.metrics.message_page_duration),
  };
  return {
    stdout: `${JSON.stringify(result, null, 2)}\n`,
    [summaryPath]: `${JSON.stringify(result, null, 2)}\n`,
  };
}
