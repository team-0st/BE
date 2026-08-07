import http from "k6/http";
import { check, sleep } from "k6";

const baseUrl = __ENV.BASE_URL || "https://dev-api.zero-st.com";
const accessToken = __ENV.ACCESS_TOKEN;

if (!accessToken) {
  throw new Error("ACCESS_TOKEN environment variable is required.");
}

function parseVuValue(envName, defaultValue) {
  const rawValue = __ENV[envName];
  const parsedValue = rawValue === undefined || rawValue === "" ? defaultValue : Number(rawValue);

  if (!Number.isFinite(parsedValue) || !Number.isInteger(parsedValue) || parsedValue < 0) {
    throw new Error(`${envName} must be a non-negative integer.`);
  }

  return parsedValue;
}

function constantVuScenario(vusEnvName, defaultVus, durationEnvName, defaultDuration, startTimeEnvName, defaultStartTime, tag) {
  const vus = parseVuValue(vusEnvName, defaultVus);
  if (vus <= 0) {
    return null;
  }

  return {
    executor: "constant-vus",
    vus,
    duration: __ENV[durationEnvName] || defaultDuration,
    exec: "getMissionList",
    startTime: __ENV[startTimeEnvName] || defaultStartTime,
    tags: { scenario: tag },
  };
}

const scenarios = Object.fromEntries(
  Object.entries({
    smoke_single_user: constantVuScenario(
      "SMOKE_VUS",
      1,
      "SMOKE_DURATION",
      "30s",
      "SMOKE_START_TIME",
      "0s",
      "smoke_single_user",
    ),
    steady_small_load: constantVuScenario(
      "SMALL_VUS",
      10,
      "SMALL_DURATION",
      "1m",
      "SMALL_START_TIME",
      "35s",
      "steady_small_load",
    ),
    steady_medium_load: constantVuScenario(
      "MEDIUM_VUS",
      30,
      "MEDIUM_DURATION",
      "1m",
      "MEDIUM_START_TIME",
      "1m40s",
      "steady_medium_load",
    ),
    spike_load: constantVuScenario(
      "SPIKE_VUS",
      50,
      "SPIKE_DURATION",
      "30s",
      "SPIKE_START_TIME",
      "2m45s",
      "spike_load",
    ),
  }).filter(([, scenario]) => scenario !== null),
);

if (Object.keys(scenarios).length === 0) {
  throw new Error("At least one scenario must have VUs greater than 0.");
}

export const options = {
  scenarios,
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<1000"],
    checks: ["rate==1"],
  },
};

export function getMissionList() {
  const response = http.get(`${baseUrl}/api/v1/missions`, {
    headers: {
      Authorization: `Bearer ${accessToken}`,
      "X-Request-Id": `k6-mission-list-${__VU}-${__ITER}`,
    },
    tags: {
      endpoint: "GET /api/v1/missions",
    },
  });

  check(response, {
    "status is 200": (res) => res.status === 200,
    "success=true": (res) => {
      try {
        return JSON.parse(res.body).success === true;
      } catch (_) {
        return false;
      }
    },
  });

  sleep(Number(__ENV.SLEEP_SECONDS || 0.2));
}
