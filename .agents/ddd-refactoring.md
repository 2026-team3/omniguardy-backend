# DDD package rules

Each bounded context under `com.omniguardy.backend.domain` follows these dependency rules:

- `presentation` depends on `application` and maps HTTP DTOs at the boundary.
- `application` coordinates use cases and owns inbound/outbound ports.
- `domain` owns entities, value objects, policies, and repository contracts.
- `infrastructure` implements outbound ports for persistence and external systems.
- `domain` must not depend on presentation or infrastructure types.

Cross-context collaboration goes through application use cases or explicit ports.
Shared Spring configuration, API envelopes, and exception handling remain under `global`.
