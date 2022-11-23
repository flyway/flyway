---
pill: websiteSetup
subtitle: Dev Environment Setup - Website
---
<div id="websiteSetup">
    <h1>Documentation: Dev Environment Setup</h1>

    <p>To contribute to the Flyway documentation site you will need to set up your development environment so that you run the
        website locally and submit changes. </p>

    <p>For this you will need to set up Git, Make, Docker and an editor.</p>

    <h2>Prerequisites</h2>

    <ul>
        <li><a href="https://git-scm.com/downloads">Git</a></li>
        <li><a href="https://docs.docker.com/install/">Docker</a></li>
        <li>
            Make:
            <ul>
                <li>Windows: <code>choco install make</code></li>
                <li>Ubuntu: <code>sudo apt install make</code></li>
                <li>macOS: <code>xcode-select --install</code> (available as part of the Command Line Tools for Xcode)</li>
            </ul>
        </li>
    </ul>

    <h3>Editor</h3>

    <p>The documentation site uses HTML files with Liquid headers for Jekyll. We personally use IntelliJ, which works quite well,
        but feel free to use any editor that suits you.</p>

    <h2>Done!</h2>

    <p>Congratulations! You now have a fully functional dev environment for the Flyway documentation site. You can start
        contributing!</p>
</div>
