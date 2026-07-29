# Contributing to FleetOps

First off, thank you for considering contributing to FleetOps! It's people like you that make open-source software such a powerful tool.

## Where do I go from here?

If you've noticed a bug or have a feature request, make sure to check the [Issues](https://github.com/KrupalDusane/FleetOps/issues) tab to see if it has already been reported. If not, open a new issue!

## Fork & create a branch

1. Fork the repository.
2. Create your feature branch (`git checkout -b feat/amazing-feature`).
3. Commit your changes (`git commit -m 'feat: Add some amazing feature'`).
4. Push to the branch (`git push origin feat/amazing-feature`).
5. Open a Pull Request.

## Pull Request Guidelines

Before submitting a Pull Request, please ensure the following:
- **Compile:** The code compiles successfully using `mvn clean compile`.
- **Tests (If applicable):** Any new logic is covered by unit tests.
- **Naming Conventions:** Follow standard Java naming conventions and maintain the existing layered architecture.
- **Commit Messages:** We follow conventional commit messages (`feat:`, `fix:`, `docs:`, `refactor:`).

## Code Style

- FleetOps uses standard Java formatting.
- Ensure no trailing whitespaces and file endings are `LF`.
- Use explicit dependency injection via constructors instead of `@Autowired` fields.

By contributing to FleetOps, you agree that your contributions will be licensed under its MIT License.
