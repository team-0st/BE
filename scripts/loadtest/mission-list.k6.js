import http from "k6/http";
import { check, sleep } from "k6";

const baseUrl = __ENV.BASE_URL || "https://dev-api.zero-st.com";
const accessToken = __ENV.ACCESS_TOKEN;

if (!accessToken) {
  throw new Error("ACCESS_TOKEN environment variable is required.");
}

export const options = {
  scenarios: {
    smoke_single_user: {
      executor: "constant-vus",
      vus: Number(__ENV.SMOKE_VUS || 1),
      duration: __ENV.SMOKE_DURATION || "30s",
      exec: "getMissionList",
      tags: { scenario: "smoke_single_user" },
    },
    steady_small_load: {
      executor: "constant-vus",
      vus: Number(__ENV.SMALL_VUS || 10),
      duration: __ENV.SMALL_DURATION || "1m",
      exec: "getMissionList",
      startTime: __ENV.SMALL_START_TIME || "35s",
      tags: { scenario: "steady_small_load" },
    },
    steady_medium_load: {
      executor: "constant-vus",
      vus: Number(__ENV.MEDIUM_VUS || 30),
      duration: __ENV.MEDIUM_DURATION || "1m",
      exec: "getMissionList",
      startTime: __ENV.MEDIUM_START_TIME || "1m40s",
      tags: { scenario: "steady_medium_load" },
    },
    spike_load: {
      executor: "constant-vus",
      vus: Number(__ENV.SPIKE_VUS || 50),
      duration: __ENV.SPIKE_DURATION || "30s",
      exec: "getMissionList",
      startTime: __ENV.SPIKE_START_TIME || "2m45s",
      tags: { scenario: "spike_load" },
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<1000"],
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
