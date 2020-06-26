# JOPA Example 04 - JOPA + Spring

This example shows integration of JOPA with a CDI framework (Spring in this case).

### Features

- Named native queries
- Spring integration

This example does not show any other significant JOPA features. To see more features, consult the other examples. This
demo mainly shows integration of JOPA with CDI frameworks like Spring.

#### Named native queries

The `cz.cvut.kbss.jopa.example04.model.Student` contains a declaration of two named native queries - a _find all_ query and
a query selecting by a key. The second query shows how parameters can be set in named native queries when used in a DAO.
See `cz.cvut.kbss.jopa.example04.persistence.dao.StudentDao` for example of the named native query usage.

#### EntityManagerFactory as a Spring Bean

Most importantly, JOPA's `EntityManagerFactory` (EMF) is created as a Spring bean and managed by the Spring context. The class 
`cz.cvut.kbss.jopa.example04.persistence.PersistenceFactory` after its initialization instantiates EMF and provides it as a bean. 
The EMF instance is closed before application shutdown.

#### EntityManager in Data Access Objects

Class `cz.cvut.kbss.jopa.example04.persistence.dao.StudentDao` shows usage of the EMF bean. In each DAO method, a fresh `EntityManager` instance
is requested, used to perform persistence logic and then discarded. The `try-finally` pattern ensures that the entity manager is always
closed correctly.

#### Declarative Transactions

Thanks to the [JOPA-Spring-transaction](https://github.com/ledsoft/jopa-spring-transaction) library, it is possible to use
declarative transaction demarcation (`@Transactional`) with JOPA. See `StudentService` for an example.

## Persistence Setup

The persistence is set up in `cz.cvut.kbss.jopa.example04.persistence.PersistenceFactory`. Sesame native storage location is specified
in `config.properties` in `src/main/resources`. Note that the path expects that the application is running on Linux. Feel free to specify
a different storage location (including remote repositories, just remember to adhere to the Sesame repository naming scheme).

## Running the Demo

This demo is a web application with UI written in Javascript (using ReactJS). To run the application, run `mvn package` and deploy
the resulting war file into any Java EE 7 Web Profile-compliant server (Apache Tomcat, Jetty, Glassfish etc.).
The UI will use a prebuilt file `src/main/webapp/js/bundle.js`.

In order to modify the application, NodeJS has to be installed on the system. Go to `src/main/webapp` and run `npm install` to install the JS
dependencies. By executing `npm start`, a watcher will be started which rebuilds the UI archive whenever a change is made to the JS files.

The web UI is built with [ReactJS](https://facebook.github.io/react/) and uses its [Flux](https://facebook.github.io/flux/docs/overview.html)
architecture (modified version, framework [Reflux](https://github.com/reflux/refluxjs)). 
Individual JS files contain some additional explanatory comments.
