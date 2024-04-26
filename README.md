[![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)](https://GitHub.com/Naereen/StrapDown.js/graphs/commit-activity)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)

[![name](https://github.com/modrinth/art/blob/main/Branding/Badge/badge-dark__184x72.png?raw=true)](https://modrinth.com/mod/biscuit!)

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/G2G4DZF4D)

<img src="https://raw.githubusercontent.com/MrNavaStar/Biscuit/master/src/main/resources/assets/biscuit/biscuit.png" width="300" height="300">


# Biscuit!

A simple API for working with 1.20.5 cookies and transfers in an easy and secure way!

## Features

- [x] Cookies as POJOs
- [x] Injected interfaces on many networking classes
- [x] Cookie Signing for tamper detection
- [x] Event system
- [ ] Automatic identifier generation (May not be possbile)
- [ ] Encryption
- [ ] Spreading large data across many cookies

# Setup
In your build.gradle include:
``` gradle
repositories {
    maven { url "https://api.modrinth.com/maven" }
}

dependencies {
  modImplementation("maven.modrinth:biscuit!:1.0.0")
}
```

## Dev

```java
// Make a class to represent your cookie
public static class TestCookie {

    private final String data;
    
    public TestCookie(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }
}

// Register the cookie:
Biscuit.register(new Identifier("test", "cookie"), TestCookie.class);

// Register the cookie with a secret for tamper detection. The secret must be the same on all servers requesting this cookie:
Biscuit.register(new Identifier("test", "cookie"), TestCookie.class).setSecret("my_secret");

// Set/Get the cookie from the ServerPlayerEntity, ClientConnection, or any of the network handlers (Not handshake)
TestCookie cookie = new TestCookie("whoohoo!");
player.setCookie(cookie);

player.getCookie(TestCookie.class).whenComplete((cookie, throwable) -> {
    System.out.println(cookie);
});
```

Set a cookie before your mod (or another mod/the transfer command) transfers the player:
```java
BiscuitEvents.PRE_TRANSFER.register((packet, profile, cookieJar, ci) -> {
    cookieJar.setCookie(cookie);
});
```
