import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

const baseUrl = (__ENV.BASE_URL || 'http://api:8080').replace(/\/$/, '');
const jobId = requiredEnvironment('JOB_ID');
const expectedCompanyId = requiredEnvironment('EXPECTED_COMPANY_ID');
const integrationToken = requiredEnvironment('INTEGRATION_TOKEN');
const missingJobId = __ENV.MISSING_JOB_ID || '00000000-0000-0000-0000-000000000000';
const runNumber = Number(__ENV.RUN_NUMBER || '1');
const summaryPath = __ENV.SUMMARY_PATH || `/spike/resultados/run-${runNumber}.json`;

const measuredRequests = new Counter('measured_requests');
const measuredContractValid = new Rate('measured_contract_valid');
const measuredContractDuration = new Trend('measured_contract_duration', true);
const positiveProbeValid = new Rate('positive_probe_valid');
const missingTokenRejected = new Rate('missing_token_rejected');
const invalidTokenRejected = new Rate('invalid_token_rejected');
const missingJobNotFound = new Rate('missing_job_not_found');

http.setResponseCallback(http.expectedStatuses({ min: 200, max: 299 }, 401, 404));

export const options = {
  scenarios: {
    conversation_context: {
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
    measured_contract_valid: ['rate==1'],
    measured_contract_duration: ['p(95)<=500'],
    positive_probe_valid: ['rate==1'],
    missing_token_rejected: ['rate==1'],
    invalid_token_rejected: ['rate==1'],
    missing_job_not_found: ['rate==1'],
  },
  summaryTrendStats: ['min', 'avg', 'med', 'p(95)', 'max'],
};

function requiredEnvironment(name) {
  const value = __ENV[name];
  if (!value) {
    throw new Error(`Falta la variable de entorno obligatoria ${name}`);
  }
  return value;
}

function endpoint(id) {
  return `${baseUrl}/internal/integration/v1/jobs/${id}/conversation-context`;
}

function request(id, token) {
  const headers = { Accept: 'application/json' };
  if (token !== null) {
    headers['X-Integration-Token'] = token;
  }
  return http.get(endpoint(id), {
    headers,
    tags: { operation: 'conversation_context' },
  });
}

function hasExactContract(result) {
  if (result.status !== 200) return false;
  try {
    const body = result.json();
    const keys = Object.keys(body).sort().join(',');
    return (
      keys === 'companyId,jobId' &&
      String(body.jobId).toLowerCase() === jobId.toLowerCase() &&
      String(body.companyId).toLowerCase() === expectedCompanyId.toLowerCase()
    );
  } catch (_) {
    return false;
  }
}

export function setup() {
  const positive = request(jobId, integrationToken);
  const positiveOk = check(positive, {
    'positive probe returns the exact minimal contract': hasExactContract,
  });
  positiveProbeValid.add(positiveOk);

  const withoutToken = request(jobId, null);
  const missingTokenOk = check(withoutToken, {
    'missing integration token returns 401': (result) => result.status === 401,
  });
  missingTokenRejected.add(missingTokenOk);

  const withInvalidToken = request(jobId, `${integrationToken}-invalid`);
  const invalidTokenOk = check(withInvalidToken, {
    'invalid integration token returns 401': (result) => result.status === 401,
  });
  invalidTokenRejected.add(invalidTokenOk);

  const unknownJob = request(missingJobId, integrationToken);
  const missingJobOk = check(unknownJob, {
    'unknown job returns 404': (result) => result.status === 404,
  });
  missingJobNotFound.add(missingJobOk);

  return { setupChecksPassed: positiveOk && missingTokenOk && invalidTokenOk && missingJobOk };
}

export default function () {
  const response = request(jobId, integrationToken);
  const valid = check(response, {
    'measured response returns the exact minimal contract': hasExactContract,
  });
  measuredRequests.add(1);
  measuredContractValid.add(valid);
  measuredContractDuration.add(response.timings.duration);
}

function metricValue(data, metricName, valueName, fallback = null) {
  return data.metrics[metricName]?.values?.[valueName] ?? fallback;
}

function thresholdPassed(data, metricName) {
  const thresholds = data.metrics[metricName]?.thresholds || {};
  return Object.values(thresholds).every((threshold) => threshold.ok);
}

export function handleSummary(data) {
  const metricNames = [
    'checks',
    'http_req_failed',
    'measured_requests',
    'measured_contract_valid',
    'measured_contract_duration',
    'positive_probe_valid',
    'missing_token_rejected',
    'invalid_token_rejected',
    'missing_job_not_found',
  ];
  const thresholds = Object.fromEntries(
    metricNames.map((name) => [name, thresholdPassed(data, name)]),
  );
  const result = {
    schema_version: 1,
    run: runNumber,
    discarded: runNumber === 1,
    captured_at_utc: new Date().toISOString(),
    tool: 'k6',
    scenario: 'conversation_context',
    executor: 'shared-iterations',
    virtual_users: 10,
    requests: metricValue(data, 'measured_requests', 'count', 0),
    total_http_requests: metricValue(data, 'http_reqs', 'count', 0),
    checks_rate: metricValue(data, 'checks', 'rate', 0),
    failed_request_rate: metricValue(data, 'http_req_failed', 'rate', 1),
    measured_contract_valid_rate: metricValue(data, 'measured_contract_valid', 'rate', 0),
    negative_checks: {
      missing_token_rejected_rate: metricValue(data, 'missing_token_rejected', 'rate', 0),
      invalid_token_rejected_rate: metricValue(data, 'invalid_token_rejected', 'rate', 0),
      missing_job_not_found_rate: metricValue(data, 'missing_job_not_found', 'rate', 0),
    },
    positive_probe_valid_rate: metricValue(data, 'positive_probe_valid', 'rate', 0),
    latency_ms: {
      min: metricValue(data, 'measured_contract_duration', 'min'),
      mean: metricValue(data, 'measured_contract_duration', 'avg'),
      p50: metricValue(data, 'measured_contract_duration', 'med'),
      p95: metricValue(data, 'measured_contract_duration', 'p(95)'),
      max: metricValue(data, 'measured_contract_duration', 'max'),
    },
    thresholds,
    thresholds_passed: Object.values(thresholds).every(Boolean),
  };
  return {
    stdout: `${JSON.stringify(result, null, 2)}\n`,
    [summaryPath]: `${JSON.stringify(result, null, 2)}\n`,
  };
}
