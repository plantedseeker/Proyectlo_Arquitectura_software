import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

const baseUrl = __ENV.BASE_URL || 'http://api:8080';
const chatId = __ENV.CHAT_ID || '3fb4ce25-b840-4685-d2ea-53f9a4bdedc6';
const runNumber = Number(__ENV.RUN_NUMBER || '1');
const summaryPath = __ENV.SUMMARY_PATH || `/experiment/resultados/run-${runNumber}.json`;
const measuredRequests = new Counter('measured_requests');
const validMessagePage = new Rate('valid_message_page');
const messagePageDuration = new Trend('message_page_duration', true);

export const options = {
  scenarios: {
    latest_messages: {
      executor: 'shared-iterations',
      vus: 10,
      iterations: 40,
      maxDuration: '30s',
    },
  },
  thresholds: {
    checks: ['rate==1'],
    http_req_failed: ['rate==0'],
    measured_requests: ['count==40'],
    valid_message_page: ['rate==1'],
    message_page_duration: ['p(95)<=500'],
  },
  summaryTrendStats: ['min', 'avg', 'med', 'p(95)', 'max'],
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

export default function (data) {
  const response = http.get(
    `${baseUrl}/api/chats/${chatId}/messages?limit=50&offset=0`,
    {
      headers: {
        Authorization: `Bearer ${data.token}`,
        Accept: 'application/json',
      },
      tags: { operation: 'latest_50_messages' },
    },
  );

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
  return Object.values(metric.thresholds || {}).every((threshold) => threshold.ok);
}

export function handleSummary(data) {
  const duration = data.metrics.message_page_duration.values;
  const result = {
    schema_version: 1,
    run: runNumber,
    discarded: runNumber === 1,
    captured_at_utc: new Date().toISOString(),
    tool: 'k6',
    scenario: 'latest_messages',
    requests: data.metrics.measured_requests.values.count,
    checks_rate: data.metrics.checks.values.rate,
    failed_request_rate: data.metrics.http_req_failed.values.rate,
    valid_page_rate: data.metrics.valid_message_page.values.rate,
    min_ms: duration.min,
    mean_ms: duration.avg,
    p50_ms: duration.med,
    p95_ms: duration['p(95)'],
    max_ms: duration.max,
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
