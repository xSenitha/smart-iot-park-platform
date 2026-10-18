# Contributing to Smart IoT Park Platform

Thank you for your interest in contributing to the **Smart IoT Park Platform**!

## Code of Conduct
We are committed to providing a welcoming, inclusive, and harassment-free environment for all contributors.

## Branching Strategy & Workflow
1. **Default Branch**: `main` represents production-ready code.
2. **Feature Branches**: Create focused branches from `main` using the following naming conventions:
   - `feat/<feature-name>` (e.g. `feat/core-api-device-registry`)
   - `fix/<bug-name>` (e.g. `fix/ai-engine-zero-division`)
   - `docs/<doc-name>` (e.g. `docs/adr-polyglot-persistence`)
3. **Commit Messages**: We strictly adhere to [Conventional Commits](https://www.conventionalcommits.org/):
   - `feat(core-api): add zone CRUD endpoints`
   - `fix(ai-engine): handle empty sensor window`
   - `chore(ci): add maven cache`
   - `docs(readme): add architecture diagram`

## Local Development Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/<owner>/smart-iot-park-platform.git
   cd smart-iot-park-platform
   ```
2. Copy environment file:
   ```bash
   cp .env.example .env
   ```
3. Run the complete local stack:
   ```bash
   make up
   ```
4. Access the dashboard at `http://localhost:3000` and API Gateway at `http://localhost:8080`.

## Testing Standards
- All changes must include unit and integration tests.
- Target coverage is **70%+** on core logic.
- Run the full test suite before submitting PRs:
  ```bash
  make test
  ```

## Security Best Practices
- **NEVER** commit `.env`, credentials, private keys, or passwords.
- Always check `git diff --staged` before committing.
