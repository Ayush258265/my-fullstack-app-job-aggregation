# JobFinder — Job Search & AI Interview Platform

A full-stack job aggregator that pulls, deduplicates, and matches job listings from multiple sources — plus a built-in live AI mock-interview and English-speaking practice module for logged-in users.

**Live App:** https://my-fullstack-app-job-aggregation.vercel.app/
**Repo:** https://github.com/Ayush258265/my-fullstack-app-job-aggregation

## 🎥 Demo Video

https://github.com/user-attachments/assets/103cf1b6-829b-4dc4-98e5-50096aef0015


## ✨ Features

- 🔍 **Search & apply without login** — browse and apply to jobs instantly, no account required
- 🔄 **Multi-source aggregation** — pulls listings from 6+ external APIs (Arbeitnow, Remotive, RemoteOK, Adzuna, Greenhouse, Lever) with SHA-256 deduplication and a scheduled 6-hour refresh
- 🎯 **Smart job matching** — weighted algorithm (Skills 50% · Experience 35% · Location 15%) scores jobs against a user's profile
- 🔐 **JWT authentication** — secure register/login with BCrypt password hashing and role-based access (User/Admin)
- 📋 **Application tracking** — logged-in users can track jobs they've applied to, with automatic 30-day expiry cleanup
- 🎙️ **Live AI mock interview & English practice** — powered by the Grok API + Web Speech API, generating real-time topic-wise questions, scoring answers, and supporting voice input/output
- 🛠️ **Admin dashboard** — for managing listings and users

## 🧱 Tech Stack

| Layer          | Technology                                              |
|----------------|----------------------------------------------------------|
| Frontend       | React 18, Vite, React Router, Axios, Tailwind CSS         |
| Backend        | Java, Spring Boot, Spring MVC, Spring Security           |
| Database       | MySQL (Hibernate/JPA), hosted on Aiven Cloud              |
| Auth           | JWT-based authentication                                  |
| AI/Voice       | Grok API, Web Speech API                                   |
| Deployment     | Vercel (frontend), Render (backend), Aiven (database)      |

## 🖥️ What You Need to Run This Project

- **Node.js (v18+)** — runs the React frontend. Check: `node -v` · Install: https://nodejs.org
- **Java (JDK 17+)** — runs the Spring Boot backend. Check: `java -version` · Install: https://adoptium.net
- **Maven** — builds/runs the backend (`mvn spring-boot:run`); usually bundled with IntelliJ/Eclipse/STS, or install separately: https://maven.apache.org
- **MySQL database on Aiven** — you'll need your own Aiven MySQL connection URL, username, and password (or point to a local MySQL instance)
- **Grok API key** — required for the AI mock interview & English-practice module. Get one from: https://x.ai/api
- **Git** — to clone the repo. Check: `git --version`
- **Code editor (optional)** — VS Code for frontend, IntelliJ IDEA / Eclipse / STS for backend

## 🚀 Getting Started

### Backend setup
```bash
git clone https://github.com/Ayush258265/my-fullstack-app-job-aggregation.git
cd my-fullstack-app-job-aggregation/backend
# configure application.properties with your Aiven MySQL credentials and API keys
mvn spring-boot:run
```

### Frontend setup
```bash
cd my-fullstack-app-job-aggregation/frontend
npm install
npm run dev
```

### Environment variables
VITE_API_BASE_URL=http://localhost:8080/api
JWT_SECRET=your_jwt_secret
GROK_API_KEY=your_grok_api_key
DB_URL=your_aiven_mysql_url
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password


## 📌 Roadmap

- [x] Job aggregation engine (6+ APIs, dedup, scheduled fetch)
- [x] Smart matching algorithm
- [x] JWT auth + role-based access
- [x] Application tracking
- [x] AI mock interview & English practice module
- [ ] Premium tier
- [ ] Admin analytics dashboard
- [ ] Production hardening & polish

## 👤 Author

**Ayush**
📧 ayush1406pal@gmail.com
🔗 [LinkedIn](https://linkedin.com/in/ayush-a67a49407/) · [GitHub](https://github.com/Ayush258265)
