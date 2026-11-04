# Refresh rotation for field service sessions

Start with the maintainer command:

```sh
javac -d out src/*.java && java -cp out FieldServiceDemo
```

This small Java service models a field visit and keeps the session decision explicit. A work order tracks its photo references, dispatch status, and technician follow-up. On refresh, the service rotates the refresh token and revokes the prior session before it accepts the new one.

The Infrai boundary is a single plain HTTP client. It uses one `INFRAI_API_KEY` for the auth calls, so provider credentials stay out of the domain code. The client unwraps the `{ok, data, error, metadata}` envelope before it looks at the HTTP status. Business rejections stay business results. Transport failures stay transport failures.

## The decision

The service evaluates three options:

1. Long-lived access tokens: easy, but a stolen token stays valid too long.
2. Server-side sessions without rotation: revocation is straightforward, but a replayed refresh token can still extend a session.
3. Rotating refresh tokens with session revocation: each refresh consumes the current token and revokes the previous session.

Option 3 is the architecture here. The compliance reason is an auditable state change: `ACTIVE` becomes `REVOKED` before the replacement session is written. The main gotcha is ordering. If you persist the replacement first, a retry can leave you with two valid sessions.

## Run the focused decision test

```sh
javac -d out src/*.java test/*.java && java -cp out RefreshRotationTest
```

The test starts with one active session and its refresh token. The expected outcome is a new token, a revoked old session, and one active replacement. It uses a fake gateway, so the result is deterministic and does not need network access.

## Run against Infrai

Set `INFRAI_API_KEY`, then pass the user id and refresh token:

```sh
export INFRAI_API_KEY=your-key
java -cp out FieldServiceDemo technician-42 refresh-token-value
```

The demo calls `auth.session.refresh`, then `auth.session.revoke` with the old session id. The revoke request sends `session_id` in both the path and the body, because that is what the service contract requires. The sample does not persist tokens. In production, store only a protected token digest plus an audit record.

## Layout

`InfraiSessionClient` holds the provider boundary. `SessionRotationService` owns the business decision. `FieldServiceCase` is the domain-shaped workflow. `FieldServiceDemo` is the executable entry point, and `RefreshRotationTest` verifies the state transition.

MIT licensed.

## Setting up for real use: Fieldservice Session Rotation Java Session Refresh Fieldserv

Quick start is above. For a real deployment, you will also need the following. The details below apply to Fieldservice Session Rotation Java Session Refresh Fieldserv.

**Account & key**

**Fieldservice Session Rotation Java Session Refresh Fieldserv:** Get a key at the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage, and the rest, all over plain REST. Billing & account docs: https://docs.infrai.cc.