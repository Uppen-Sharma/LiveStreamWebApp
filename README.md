# 🎥 Live Streaming Web Application

A feature-rich, real-time **Live Streaming Web Application** built with **Spring Boot**, **MySQL (XAMPP)**, **OBS Studio**, **NGINX RTMP**, and **FFmpeg**. This application enables seamless HLS-based video broadcasting and includes interactive features like 💬 **chat**, 👍 **like/dislike ratio**, 📊 **audience analytics**, ✂️ **highlight clips**, ⚙️ **quality control**, and 💾 **recording storage**. The frontend is styled using **Thymeleaf** and powered by **Video.js** for live playback.

> ⚠️ **Note**: MySQL (via XAMPP) is **not required** for viewing the stream only. It is **required** for dynamic features such as Like/Dislike, Chat, Realtime Analytics, Management Dashboard, Audience Graphs, and Engagement Metrics.

---

## 🛠️ Tech Stack

| 🔧 Layer        | 💻 Tools & Frameworks                                 |
|----------------|--------------------------------------------------------|
| Backend        | Java 17, Spring Boot, Maven                           |
| Frontend       | Thymeleaf, Tailwind CSS, HTML5, JavaScript, Video.js  |
| Streaming      | OBS Studio, NGINX RTMP Module, FFmpeg (HLS Encoding)  |
| Database       | MySQL via XAMPP                                        |
| Development    | VS Code / IntelliJ, JDK 17, XAMPP                     |

---

## 🚀 Features

- 🔴 Real-time HLS live streaming  
- 👍 Like/Dislike functionality with engagement ratio  
- 💬 Real-time chat system  
- 📊 Viewer analytics and audience insights  
- 🎞️ Highlight clips and short recordings  
- 💾 Stream recording and local storage  
- ⚙️ Quality control (e.g., 360p, 720p)  
- 🔐 Admin dashboard for stream/session management

---

## 📦 Deployment Guide

### ✅ Prerequisites

> ⚡ **Using XAMPP**  
> ❌ *Not required* for just viewing stream  
> ✅ *Required* for dynamic features like 💬 chat, 👍 likes, 📊 analytics, etc.

---

### 🔧 1. Install Required Tools

- 📝 VS Code or IntelliJ  
- 🗃️ XAMPP (MySQL + Apache)  
- ☕ Java JDK 17 & Java Compiler (`javac`)  
- 📦 Apache Maven  
- 🧰 Spring Boot  
- 📡 NGINX with RTMP Module  
- 📺 OBS Studio  
- 🎞️ FFmpeg  
- 🎬 Video.js (via CDN or local)

---

### 🗄️ 2. Setup MySQL Database (XAMPP)

- Start **Apache** and **MySQL** in XAMPP  
- Visit: [http://localhost/phpmyadmin](http://localhost/phpmyadmin)  
- Create database: `stream.db`  
- Update your `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/stream.db
spring.datasource.username=root
spring.datasource.password=
````

---

### 🌐 3. Configure NGINX for RTMP Streaming

Add this to your `nginx.conf`:

```nginx
rtmp {
    server {
        listen 1935;
        chunk_size 4096;
        application live {
            live on;
            record off;
        }
    }
}

http {
    server {
        listen 8081;
        location /hls {
            types {
                application/vnd.apple.mpegurl m3u8;
                video/mp2t ts;
            }
            root /path/to/hls;
            add_header Cache-Control no-cache;
        }
    }
}
```

* 🔁 Restart NGINX to apply changes

---

### 📺 4. Configure OBS Studio

* Open OBS Studio
* Go to **Settings > Stream**
* Set Service to: `Custom`
* Set Server to: `rtmp://localhost/live`
* Set Stream Key to: `stream`
* Start streaming 🎥

---

### 🎬 5. Integrate Video.js in Frontend

Add this to your HTML/Thymeleaf page:

```html
<video id="live-video" class="video-js vjs-default-skin" controls autoplay>
    <source src="http://localhost/hls/stream.m3u8" type="application/x-mpegURL">
</video>
<script src="https://vjs.zencdn.net/8.0.0/video.min.js"></script>
```

---

### ▶️ 6. Run the Spring Boot App

Open terminal and run:

```bash
cd your-project-directory
mvn spring-boot:run
```

* Access it at: [http://localhost:8080/live](http://localhost:8080/live)

---

## 📁 Project Structure

```
src/
└── main/
    ├── java/
    │   └── com/livestream/
    │       ├── controller/
    │       ├── model/
    │       ├── service/
    │       └── config/
    └── resources/
        ├── static/
        ├── templates/
        └── application.properties
```
![Image](https://github.com/user-attachments/assets/8a98a0c2-8138-43d6-86f2-4791aa70e17e)
![Image](https://github.com/user-attachments/assets/cbc0adf7-f88b-4a20-9d4e-c7bbdddd6efd)
![Image](https://github.com/user-attachments/assets/a9ad8fed-a1a2-4d48-b829-e131750140f3)
![Image](https://github.com/user-attachments/assets/147542ed-0ce4-430e-bd26-e061946afc47)
---

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the repo
2. Create a new branch
3. Make your changes
4. Submit a pull request

---

## 📬 Contact

* 📧 Email: [binayuppensharma@gmail.com](mailto:binayuppensharma@gmail.com)
* 🔗 GitHub: [LiveStreamWebApp](https://github.com/Uppen-Sharma/LiveStreamWebApp)

---

⭐ If you found this project helpful, feel free to give it a star!

```
