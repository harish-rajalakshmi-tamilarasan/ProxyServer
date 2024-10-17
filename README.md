Custom Proxy Server
Overview

This is a fun project implements a Forwarding Proxy Server in Java, designed to handle both HTTP and HTTPS requests. The proxy server is equipped with features such as logging request details into a PostgreSQL database and applying configurable rules to blacklist requests for enhanced security.
Features

    HTTP/HTTPS Support: Handles both HTTP and HTTPS requests seamlessly.
    Multithreaded: Utilizes Java's multithreading capabilities to handle multiple client requests concurrently.
    Logging: Integrates with a PostgreSQL database to log and track request details, providing insights into traffic and usage patterns.
    Request Blacklisting: Implements configurable rules to blacklist requests based on predefined criteria, enhancing security and traffic control.

Getting Started
Prerequisites

    Java Development Kit (JDK) 8 or higher
    PostgreSQL database

Installation

    Clone the repository:

Navigate to the project directory:
Update the database configuration:

    Modify the db.properties file located in the conf directory with your PostgreSQL connection details.

Running the Proxy Server

    Ensure your PostgreSQL server is running and the database is set up.
    Execute the proxy server:

    bash

    java serverclient.Starter

Configuration

    Configure your proxy settings in the db.properties file.
    Customize the request blacklisting rules as needed.

Usage

Once the server is running, configure your browser or application to use the proxy server. All HTTP/HTTPS requests will be routed through the proxy, allowing for logging and request filtering based on the configured rules.
Feel free to fork and experiment. 
