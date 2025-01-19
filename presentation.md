---
marp: true
title: "Présentation du travail pratique"
author: "David Schildböck"
theme: gaia
paginate: true
header: "HEIG-VD | DAI | Lab03"
footer: "20.01.2025"
backgroundColor: "#add8e6"
---

<!-- _Slide 1: Title Slide_ -->

# Practical work presentation

## CTFman &rarr; application with JAVA/Maven/Picocli/GitHub/Docker/**HTTP/Javalin/Swagger/Svelte/Azure/Ubuntu/PostgreSQL**

Authors &rarr; **David Schildböck & Kénan Augsburger & Arno Tribolet**  
Date &rarr; January 2025

Repo GitHub &rarr;
**https://github.com/shadowkudo/ctfman**

---

<!-- _Slide 2: Introduction_ -->

# Introduction

- CTFman - a web application to manage CTFs and their Challenges
- Project in relation with BDR

_In the game of hacking, curiosity is your greatest weapon, and persistence is your strongest shield_ \
by **ChatGPT**

---

<!-- _Slide 3: Demo -->

# Demo

The link to access the web application &rarr; [https://ctfman.cybernest.ch](https://ctfman.cybernest.ch)

The link to access the API with the documentation &rarr; [https://api.ctfman.cybernest.ch/swagger](https://api.ctfman.cybernest.ch/swagger)

---

<!-- _Slide 4: Implementation choices -->

# Implementation choices

---

<!--
Filler content:
Security key points
CORS = Cross Origin Resource Sharing
-->

![bg right](https://images.unsplash.com/photo-1634979149798-e9a118734e93?q=80&w=2338&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D)

# Security

- BCrypt
  - Simple to use
  - Secure
- Session
  - Cookies (and CORS)
  - Stored in database

<!--
```java
// Hash with random
BCrypt.with(new SecureRandom())
  .hashToString(6, password.toCharArray());
// Verify password
BCrypt.verifier()
  .verify(password, hashString)
  .verified;
```
-->

---

<!-- _Slide 5: Conclusion_ -->

# Conclusion

- Knowledge acquired
- Challenges overcome
- Valuable experience gained

**Questions ?**

<style>
  img[alt~='right'] {
    float: right;
    max-width: 200px;
  }
  .bottom-left-text {
    position: absolute;
    bottom: 60px;
    left: 30px;
    font-size: 0.7em;
    text-align: left;
  }
</style>

![bg right fit](presentation_assets/question.gif)

<div class="bottom-left-text">&rarr; Check out our <strong>README.md</strong> for more details!</div>

---

<!-- _Ending_ -->
