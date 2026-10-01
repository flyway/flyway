---
subtitle: Network troubleshooting
---

Flyway is a JVM application. It does not automatically pick up your operating system's proxy settings the way a
native Windows, macOS or Linux application would - the JVM has its own, separate proxy configuration. This is the
root cause of Flyway failing to reach the internet on a machine where a browser works fine.

## Automatic proxy inheritance

By default, Flyway does not look at your operating system's proxy configuration. To enable it, set the environment
variable `FLYWAY_USE_SYSTEM_PROXIES` to `true`. The Flyway command-line tool and Docker images then pass
`-Djava.net.useSystemProxies=true` to the JVM on startup. This tells the JVM to ask the operating system for its proxy configuration (Windows WinINET, macOS System
Configuration, or GNOME, depending on platform) and use it automatically. In most environments, no further
configuration is required.

This applies to the command line and Docker only. The Maven and Gradle plugins run inside the build tool's JVM
and are not affected - see [Maven and Gradle plugin users](#maven-and-gradle-plugin-users) below.

### The cost of proxy discovery

How the operating system answers depends on how it is configured, and some answers require a network lookup:

| Your configuration | What happens |
| ------------------- | ------------- |
| A proxy address entered manually | The setting is read locally. No network lookup. |
| Windows with "Automatically detect settings" enabled | The JVM asks Windows to discover a proxy using WPAD, which queries DHCP and DNS. |
| A proxy auto-config (PAC) URL | The PAC file is downloaded and evaluated. |
| No proxy configured | Nothing happens. |

Where a lookup is needed, it happens once per Flyway run rather than once per verb, so chaining verbs -
`flyway migrate info` - pays the cost a single time. Each new run starts a new JVM and looks again, so a script
calling Flyway repeatedly pays it every time. On a Windows machine using automatic detection we measured this at
roughly 90-100 milliseconds per run, which is usually small next to the time Flyway spends connecting to your
database. If your network advertises no WPAD server, the lookup has to fail before Flyway continues, and that can
take longer.

Linux is never affected, and neither are the Docker images, because the JVM only reads a manually entered proxy
on that platform.

### Turning it on

Set the environment variable `FLYWAY_USE_SYSTEM_PROXIES` to `true`. Any other value, or leaving it unset, means
Flyway does not look at the OS proxy configuration. You would want to enable it if Flyway cannot reach the
internet from a machine where your browser can, and you would rather reuse the OS proxy settings than configure
a proxy explicitly.

Leave it off if your OS proxy settings are set up incorrectly or unreachable from where Flyway is running, if you
want full control over proxy behavior via `JAVA_ARGS` (see below), or if proxy discovery is measurably slowing
down your commands, as described above.

## Manual proxy configuration

You can also configure a proxy explicitly, by setting the `JAVA_ARGS` environment variable:

| JAVA_ARGS value       | Purpose                                       |
| ---------------------- | ---------------------------------------------- |
| `http.proxyHost`       | the host name of the proxy to use for HTTP requests  |
| `http.proxyPort`       | the port of the proxy to use for HTTP requests      |
| `https.proxyHost`      | the host name of the proxy to use for HTTPS requests |
| `https.proxyPort`      | the port of the proxy to use for HTTPS requests     |

For example:

<pre class="console"><span>&gt;</span> set JAVA_ARGS=-Dhttp.proxyHost=myproxy -Dhttp.proxyPort=8080 -Dhttps.proxyHost=myproxy -Dhttps.proxyPort=8080</pre>

These explicit properties always take precedence over the OS proxy settings described above, so setting them is
safe even when `FLYWAY_USE_SYSTEM_PROXIES` is set to `true`.

### Excluding hosts from the proxy

Use `http.nonProxyHosts` to list hosts that should be reached directly, bypassing the proxy. This setting covers
both HTTP and HTTPS traffic - there is no separate `https.nonProxyHosts` property.

<pre class="console"><span>&gt;</span> set JAVA_ARGS=-Dhttp.proxyHost=myproxy -Dhttp.proxyPort=8080 -Dhttp.nonProxyHosts=*.internal.example.com</pre>

## Checking which proxy Flyway is using

Run any Flyway command with `-X` to log the proxy configuration Flyway detected at startup. This reports whether
explicit `JAVA_ARGS` properties are in effect, whether the OS proxy settings are being inherited, or whether no
proxy is configured at all.

## Maven and Gradle plugin users

The Flyway Maven and Gradle plugins run inside the build tool's own JVM, not the one used by the Flyway CLI. `JAVA_ARGS` has no
effect on them. To configure a proxy for these plugins, set `MAVEN_OPTS` or `GRADLE_OPTS` instead, using the same
`-Dhttp.proxyHost` / `-Dhttps.proxyHost` properties described above.

## Licensing firewall allowlist

If you are behind a restrictive firewall and need to allow specific Redgate licensing URLs through it (rather than,
or in addition to, configuring a proxy), see
[Licensing update - URLs to add to your Allowlist in firewall](https://productsupport.red-gate.com/hc/en-us/articles/360002374673-Licensing-update-URLs-to-add-to-your-AllowList-in-firewall).
