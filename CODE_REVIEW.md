# Code Review

Code review by the author himself.

## Codebase Critique

### Security
- Database credentials are left in the configuration file as plain text, which is extremely insecure. However, for this test exercise, it is acceptable. For real production applications, credentials should be stored in environment variables or a secure vault.
- The current database is an H2 local file database, which is not secure for production use. For real applications, a more secure database should be used, such as PostgreSQL or MySQL—preferably hosted on a separate server to minimize potential damage if some systems get compromised.

### Tests
- Backend tests are present and cover the main business logic of the application.
  - API tests use `@WebMvcTest` to test the controller layer.
  - Service tests use Mockito to mock the repository layer.
- Currently, there are no frontend tests implemented in the project.

### Readability
- The code is well-structured and follows Java conventions.
- The code is easy to read and understand.
- Naming of classes, methods, and variables is clear and descriptive.
- Documentation is present but minimal.

## Self-Code Review Checklist

### Does it Work?

- [x] Does the application run without crashing?
- [x] Have you tested all the requirements from the task description?
- [x] Are all sectors from the list in the database?
- [x] Does the form load the sectors from the database?
- [x] Does the "Save" button validate all fields?
- [x] Does it save the data correctly?
- [x] Does it refill the form with the saved data after you save?
- [x] Can you edit and re-save your data during the same session?