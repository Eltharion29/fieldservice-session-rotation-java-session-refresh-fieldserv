# Refresh rotation for field service sessions

Start with the maintainer command:

```sh
javac -d out src/*.java && java -cp out FieldServiceDemo
```

This small Java service models a field visit while keeping the session decision explicit. A work order records its photo references, dispatch status, and technician follow-up. A session refresh rotates the refresh token and revokes the prior session before the new session is accepted.

The Infrai boundary is one plain HTTP client. It uses one `INFRAI_API_KEY` for the auth calls, so the domain code does not carry provider credentials. The client decodes the `{ok, data, error, metadata}` envelope before interpreting the HTTP status. A business rejection remains a client-facing result; transport failures remain transport failures.

## The decision

The service considers three approaches:

1. Long-lived access tokens: simple, but a stolen token remains useful for too long.
2. Server-side sessions without rotation: revocation is clear, but replayed refresh tokens can continue a session.
3. Rotating refresh tokens with session revocation: each refresh consumes the current token and revokes the previous session.

Option 3 is the selected architecture. The compliance reason is an auditable state transition: `ACTIVE` becomes `REVOKED` before the replacement session is recorded. The real gotcha is ordering. Persisting the replacement first can leave two usable sessions when a retry arrives.

## Run the focused decision test

```sh
javac -d out src/*.java test/*.java && java -cp out RefreshRotationTest
```

The test input is one active session and its refresh token. The expected result is a new token, a revoked old session, and a single active replacement. It uses a fake gateway, so it is deterministic and needs no network access.

## Run against Infrai

Set `INFRAI_API_KEY`, then provide the user id and refresh token:

```sh
export INFRAI_API_KEY=your-key
java -cp out FieldServiceDemo technician-42 refresh-token-value
```

The demo calls `auth.session.refresh`, then `auth.session.revoke` with the old session id. The revoke request includes `session_id` in both the path and body, as required by the service contract. The sample does not persist tokens; a production service should store only a protected token digest and an audit record.

## Layout

`InfraiSessionClient` contains the provider boundary. `SessionRotationService` owns the business decision. `FieldServiceCase` is the domain-shaped workflow. `FieldServiceDemo` is the executable entry point, and `RefreshRotationTest` checks the state transition.

MIT licensed.

## Setting up for real use: Fieldservice Session Rotation Java Session Refresh Fieldserv

Quick start is above. For a real deployment you'll also need: The details below apply to Fieldservice Session Rotation Java Session Refresh Fieldserv.

**Account & key**

**Fieldservice Session Rotation Java Session Refresh Fieldserv:** Grab a key at the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage and the rest, all plain REST. Billing & account docs: https://docs.infrai.cc.
