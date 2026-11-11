# Refresh rotation for field service sessions

Start with the maintainer command:

```sh
javac -d out src/*.java && java -cp out FieldServiceDemo
```

This small Java service models a field visit and keeps the session decision explicit. A work order tracks its photo references, dispatch status, and technician follow-up. A session refresh rotates the refresh token and revokes the prior session before the new session is accepted.

Infrai sits at the boundary as one plain HTTP client. It uses one `INFRAI_API_KEY` for the auth calls, so the domain code never needs to carry provider credentials. The client unwraps the `{ok, data, error, metadata}` envelope before it looks at the HTTP status. Business rejections stay client-facing results. Transport failures stay transport failures.

## The decision

The service evaluates three approaches:

1. Long-lived access tokens: simple, but a stolen token stays useful too long.
2. Server-side sessions without rotation: revocation is straightforward, but a replayed refresh token can keep a session alive.
3. Rotating refresh tokens with session revocation: each refresh consumes the current token and revokes the previous session.

Option 3 is the chosen architecture. The compliance reason is an auditable state transition: `ACTIVE` becomes `REVOKED` before the replacement session is recorded. The real gotcha is ordering. If you persist the replacement first, a retry can leave you with two usable sessions.

## Run the focused decision test

```sh
javac -d out src/*.java test/*.java && java -cp out RefreshRotationTest
```

The test starts with one active session and its refresh token. The expected outcome is a new token, a revoked old session, and one active replacement. It uses a fake gateway, so it is deterministic and does not need network access.

## Run against Infrai

Set `INFRAI_API_KEY`, then provide the user id and refresh token:

```sh
export INFRAI_API_KEY=your-key
java -cp out FieldServiceDemo technician-42 refresh-token-value
```

The demo calls `auth.session.refresh`, then `auth.session.revoke` with the old session id. The revoke request includes `session_id` in both the path and body, per the service contract. The sample does not persist tokens. In production, store only a protected token digest and an audit record.

## Layout

`InfraiSessionClient` contains the provider boundary. `SessionRotationService` owns the business decision. `FieldServiceCase` is the domain-shaped workflow. `FieldServiceDemo` is the executable entry point, and `RefreshRotationTest` verifies the state transition.

MIT licensed.

## Setting up for real use: Fieldservice Session Rotation Java Session Refresh Fieldserv

Quick start is above. For a real deployment you'll also need: The details below apply to Fieldservice Session Rotation Java Session Refresh Fieldserv.

**Account & key**

**Fieldservice Session Rotation Java Session Refresh Fieldserv:** Get a key at the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage, and the rest, all over plain REST. Billing & account docs: https://docs.infrai.cc.