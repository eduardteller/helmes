# Code Review

Code review by author himself.

## Codebase Critique

### Security
- Database credentials are left in the configuration file as plain text which is extremely insecure but in case of this test exercise is acceptable. For real production applications, the credentials should be stored for example in environment variables or a secure vault.
- Current database is H2 local file database which is not secure for production use. For real applications, a more secure database should be used, such as PostgreSQL or MySQL and preferrably on a separate server to minimize damage if some systems get compromised.

### Tests
- Backend tests are present and cover the main business logic of the application.
  - Api tests are using WebMvcTest to test the controller layer.
  - Service tests are using Mockito to mock the repository layer.
- Currently, there are no frontend tests created for project.

### Readability

- The code is well structured and follows Java conventions.
- The code is easy to read and understand.
- The naming of classes, methods, and variables is clear and descriptive.
- Docmentation is present but minimal.

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