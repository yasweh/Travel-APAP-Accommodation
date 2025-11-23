# Jawaban Pertanyaan Praktikum CI/CD

## 1. Screenshot Deployment Berhasil

**Untuk Sidating (BE1, BE2, FE):**
- Screenshot Test-BE1: Menunjukkan hasil testing backend 1
- Screenshot Test-BE2: Menunjukkan hasil testing backend 2  
- Screenshot Test-FE: Menunjukkan hasil testing frontend

**Untuk BE (Individual Task):**
- Screenshot request Bruno/Postman dengan response valid
- Pastikan URL endpoint terlihat jelas dalam screenshot

**Untuk FE (Individual Task):**
- Screenshot halaman yang dapat diakses melalui URL hasil deployment
- Pastikan URL deployment terlihat di address bar

## 2. Pipeline CI/CD - Spring Boot

```yaml
stages:
  - build
  - test
  - deploy

build:
  stage: build
  script:
    - ./gradlew clean build -x test
  artifacts:
    paths:
      - build/libs/*.jar

test:
  stage: test
  script:
    - ./gradlew test

deploy:
  stage: deploy
  script:
    - scp build/libs/*.jar user@server:/app/
    - ssh user@server "sudo systemctl restart spring-app"
  only:
    - main
```

**Deskripsi singkat:**
- **Build stage**: Kompilasi aplikasi Spring Boot menggunakan Gradle, menghasilkan JAR file
- **Test stage**: Menjalankan unit test untuk memastikan kode berfungsi dengan baik
- **Deploy stage**: Transfer JAR ke server dan restart service (hanya untuk branch main)

## 3. Pipeline CI/CD Improvement

```yaml
stages:
  - validate
  - build
  - test
  - security-scan
  - deploy
  - monitoring

validate:
  stage: validate
  script:
    - echo "Validating code quality..."
    - ./gradlew checkstyleMain
    - sonar-scanner

build:
  stage: build
  script:
    - ./gradlew clean build -x test
  artifacts:
    paths:
      - build/libs/*.jar
    expire_in: 1 week

test:
  stage: test
  parallel:
    matrix:
      - TEST_TYPE: [unit, integration, e2e]
  script:
    - ./gradlew ${TEST_TYPE}Test
  coverage: '/Total coverage: \d+\.\d+%/'

security-scan:
  stage: security-scan
  script:
    - trivy image $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
    - dependency-check --project myapp

deploy:
  stage: deploy
  script:
    - docker build -t $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA .
    - docker push $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
    - kubectl set image deployment/myapp myapp=$CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
    - kubectl rollout status deployment/myapp
  environment:
    name: production
    url: https://myapp.example.com
  only:
    - main

monitoring:
  stage: monitoring
  script:
    - curl -X POST https://monitoring.example.com/notify
    - echo "Deployment completed at $(date)"
```

**Penjelasan improvement:**
- **Validate stage**: Code quality check dengan checkstyle dan SonarQube untuk analisis static code
- **Parallel testing**: Unit, integration, dan E2E test berjalan paralel untuk mempercepat pipeline
- **Security scan**: Vulnerability scanning pada Docker image dan dependencies
- **Rollout verification**: Memastikan deployment berhasil dengan kubectl rollout status
- **Monitoring stage**: Notifikasi ke sistem monitoring setelah deployment
- **Artifact management**: Expire artifact setelah 1 minggu untuk menghemat storage

## 4. EC2 Instance dan Elastic IP

**Mengapa mengaitkan EC2 dengan Elastic IP?**
- Instance EC2 mendapat IP publik yang **berubah setiap kali instance di-restart**
- Elastic IP memberikan **IP statis yang tidak berubah**, penting untuk:
  - DNS configuration yang stabil
  - Whitelist firewall yang konsisten
  - Client access yang tidak terganggu saat restart

**Apa yang terjadi jika tidak mengaitkan Elastic IP?**
- Setiap restart instance akan mendapat IP publik baru
- DNS record harus diupdate manual
- Client connection akan terputus
- Konfigurasi firewall/security group perlu disesuaikan ulang
- Tidak cocok untuk production environment

## 5. Perbedaan Docker dan Kubernetes

| Aspek | Docker | Kubernetes |
|-------|--------|------------|
| **Fungsi Utama** | Container runtime & image builder | Container orchestration platform |
| **Scope** | Single host | Multi-host cluster |
| **Scaling** | Manual scaling | Auto-scaling (HPA, VPA) |
| **Load Balancing** | Tidak built-in | Built-in service load balancing |
| **Self-healing** | Tidak ada | Auto restart, replacement pods |
| **Networking** | Bridge/host network | Advanced networking (CNI plugins) |
| **Storage** | Volumes | Persistent Volumes & Claims |
| **Use Case** | Development, simple deployment | Production, complex microservices |

**Dalam praktikum ini:**
- **Docker**: Digunakan untuk membuat container image aplikasi
- **Kubernetes**: Mengelola deployment, scaling, dan orchestration container di cluster

## 6. Proses Terpenting dari Pipeline

**Proses paling penting: TESTING**

**Mengapa?**
- Mencegah bug masuk ke production
- Memvalidasi fungsionalitas sebelum deploy
- Memberikan confidence bahwa code changes tidak merusak existing features
- Mendeteksi regression errors lebih awal
- Lebih murah memperbaiki bug di development daripada di production

**Proses penting lainnya:**
1. **Build**: Memastikan code dapat dikompilasi tanpa error
2. **Security Scan**: Mencegah vulnerability masuk ke production
3. **Deploy**: Mengotomasi proses delivery ke environment

## 7. Konfigurasi Kubernetes - Penjelasan 5 File

### File di Repository (k8s folder):

**1. deployment.yaml**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
spec:
  replicas: 3
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
      - name: myapp
        image: myapp:latest
        resources:
          limits:
            cpu: "500m"
            memory: "512Mi"
```
**Kegunaan**: Mendefinisikan bagaimana aplikasi di-deploy (jumlah replicas, image, resources), mengatur strategy update (RollingUpdate/Recreate)

**2. service.yaml**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: myapp-service
spec:
  type: ClusterIP
  selector:
    app: myapp
  ports:
  - port: 80
    targetPort: 8080
```
**Kegunaan**: Expose aplikasi ke network, mengatur load balancing antar pods, memberikan stable network endpoint

**3. ingress.yaml**
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: myapp-ingress
spec:
  rules:
  - host: myapp.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: myapp-service
            port:
              number: 80
```
**Kegunaan**: Mengatur routing HTTP/HTTPS dari luar cluster, SSL/TLS termination, domain-based routing

### File di .gitlab-ci.yml:

**4. secret.yaml**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: myapp-secret
type: Opaque
data:
  database-password: cGFzc3dvcmQxMjM=  # base64 encoded
  api-key: YXBpa2V5MTIzNDU2Nzg5MA==
```
**Kegunaan**: Menyimpan credentials (database password, API keys) dengan aman, base64 encoded, encrypted at rest

**5. configmap.yaml**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: myapp-config
data:
  APP_ENV: "production"
  LOG_LEVEL: "info"
  DATABASE_HOST: "postgres.default.svc.cluster.local"
```
**Kegunaan**: Menyimpan konfigurasi non-sensitif (environment variables), dapat di-mount sebagai file atau environment variable, memisahkan konfigurasi dari code

**Ringkasan Kegunaan Kelima File:**
- **Deployment**: Menjalankan aplikasi dengan spesifikasi yang ditentukan
- **Service**: Memberikan stable network endpoint untuk akses ke pods
- **Ingress**: Entry point untuk traffic eksternal dengan domain/path routing
- **Secret**: Menyimpan data sensitif dengan aman
- **ConfigMap**: Memisahkan konfigurasi dari code, memudahkan perubahan tanpa rebuild image

## 8. Start on Restart - Docker dan Kubernetes

**Perbedaan perilaku default:**

| Sistem | Default Behavior | Saat Server Restart |
|--------|------------------|---------------------|
| **Docker** | Container tidak auto-restart | Container tetap mati |
| **Kubernetes** | Pods selalu di-maintain | Pods otomatis restart |

### Docker - Start on Restart

Diterapkan menggunakan **restart policy**:

```bash
# Saat menjalankan container
docker run --restart=always myapp

# Atau di docker-compose.yml
services:
  myapp:
    restart: always
```

**Restart policy options:**
- `no`: Tidak restart (default)
- `on-failure`: Restart hanya jika exit code != 0
- `always`: Selalu restart, termasuk saat Docker daemon restart
- `unless-stopped`: Restart kecuali di-stop manual

### Kubernetes - Start on Restart

Kubernetes **secara default sudah menerapkan** konsep ini melalui:

**1. Desired State Management**
```yaml
apiVersion: apps/v1
kind: Deployment
spec:
  replicas: 3  # Kubernetes akan selalu maintain 3 pods
```

**2. RestartPolicy di Pod level:**
```yaml
spec:
  restartPolicy: Always  # Default value
```
- `Always`: Restart container saat crash (default untuk Deployment)
- `OnFailure`: Restart hanya jika exit code != 0
- `Never`: Tidak restart (untuk Job/CronJob)

**3. Node failure handling**: Jika node mati, pods akan di-reschedule ke node lain

**Bagaimana diterapkan:**
- **Docker**: Melalui Docker daemon yang memonitor container state
- **Kubernetes**: Melalui kubelet (node agent) dan controller manager yang terus-menerus reconcile desired state vs actual state

## 9. Keuntungan Kubernetes vs Docker Saja

**Keuntungan menggunakan Kubernetes:**

### 1. High Availability
- Jika satu pod crash, otomatis dibuat pod baru
- Traffic di-route ke pod yang healthy
- Zero-downtime deployment dengan rolling update

### 2. Horizontal Scaling
```bash
kubectl scale deployment myapp --replicas=10
# Atau auto-scaling berdasarkan CPU/memory
```

### 3. Load Balancing
- Traffic didistribusi otomatis ke multiple pods
- Built-in service discovery

### 4. Self-Healing
- Restart container yang gagal
- Replace dan reschedule pods saat node mati
- Health check (liveness & readiness probes)

### 5. Declarative Configuration
```yaml
# Define desired state, Kubernetes akan maintain-nya
apiVersion: apps/v1
kind: Deployment
spec:
  replicas: 3
```

### 6. Rolling Updates & Rollbacks
```bash
# Update tanpa downtime
kubectl rollout status deployment/myapp
# Rollback jika ada masalah
kubectl rollout undo deployment/myapp
```

### 7. Resource Management
- Request & limit CPU/memory per container
- Namespace untuk isolasi resources
- Priority & preemption

### 8. Multi-Cloud & Portability
- Tidak terikat vendor tertentu
- Consistent API di AWS, GCP, Azure, on-premise

**Docker saja (tanpa Kubernetes):**
- ❌ Manual scaling dan load balancing
- ❌ Tidak ada automatic recovery jika server mati
- ❌ Sulit manage di multiple servers
- ❌ Manual health checking dan restart
- ✅ Lebih sederhana untuk aplikasi kecil
- ✅ Resource overhead lebih kecil

## 10. Perbedaan Service Types di Kubernetes

### ClusterIP

**Karakteristik:**
- Default service type
- Hanya accessible dari dalam cluster
- Internal IP address yang stabil

**Kapan digunakan:**
- Communication antar services dalam cluster (microservices)
- Database, message queue, internal APIs
- Backend services yang tidak perlu exposed ke luar

**Contoh:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: backend-service
spec:
  type: ClusterIP  # Default, bisa tidak ditulis
  selector:
    app: backend
  ports:
    - port: 8080
      targetPort: 8080
```

### NodePort

**Karakteristik:**
- Expose service pada static port di setiap node (30000-32767)
- Accessible dari luar cluster melalui `<NodeIP>:<NodePort>`
- Otomatis membuat ClusterIP juga

**Kapan digunakan:**
- Development/testing environment
- Akses langsung tanpa load balancer
- On-premise cluster tanpa cloud load balancer

**Contoh:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: frontend-service
spec:
  type: NodePort
  selector:
    app: frontend
  ports:
    - port: 80
      targetPort: 3000
      nodePort: 30080  # Opsional, otomatis assign jika tidak ditulis
```

**Access:** `http://<any-node-ip>:30080`

### LoadBalancer

**Karakteristik:**
- Membuat external load balancer (cloud provider)
- Otomatis membuat NodePort dan ClusterIP
- Mendapat external IP yang stable

**Kapan digunakan:**
- Production environment di cloud (AWS, GCP, Azure)
- Perlu external access dengan high availability
- Automatic SSL termination (tergantung cloud provider)

**Contoh:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: web-service
spec:
  type: LoadBalancer
  selector:
    app: web
  ports:
    - port: 80
      targetPort: 8080
```

**Access:** `http://<external-ip>` (IP dari cloud load balancer)

### Mengapa ClusterIP untuk Praktikum Ini?

**Alasan memilih ClusterIP:**

1. **Arsitektur yang tepat**: 
   - Backend services tidak perlu exposed directly ke internet
   - Access ke backend melalui Ingress atau API Gateway

2. **Security**:
   - Mengurangi attack surface
   - Backend hanya accessible dari dalam cluster
   - Frontend/Ingress sebagai single entry point

3. **Best practice**:
   ```
   Internet → Ingress/LoadBalancer → Frontend Service (ClusterIP)
                                    → Backend Service (ClusterIP)
                                    → Database Service (ClusterIP)
   ```

4. **Cost effective**:
   - Tidak perlu multiple load balancers
   - Cukup 1 LoadBalancer/Ingress untuk entry point

5. **Service discovery**:
   - Services bisa communicate via DNS name
   - `backend-service:8080` bisa diakses dari frontend

**Struktur ideal:**
```yaml
# Ingress (single entry point)
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: main-ingress
spec:
  rules:
    - host: myapp.example.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: frontend-service  # ClusterIP
                port:
                  number: 80
          - path: /api
            pathType: Prefix
            backend:
              service:
                name: backend-service  # ClusterIP
                port:
                  number: 8080
```

## 11. Pelajaran dari Automated Deployment & Penerapan CI/CD

### Pelajaran Penting

#### 1. Automation Saves Time & Reduces Human Error
- Manual deployment prone to mistakes (typo, missing steps)
- CI/CD memastikan setiap deployment consistent dan reproducible
- Developer fokus pada coding, bukan deployment manual

#### 2. Fast Feedback Loop
- Automated testing memberikan feedback cepat
- Bug terdeteksi lebih awal di pipeline
- Faster iteration dan development cycle

#### 3. Version Control Everything
- Infrastructure as Code (IaC)
- Configuration di Git repository
- Easy rollback dan audit trail

#### 4. Testing is Non-Negotiable
- Automated testing sebagai safety net
- Prevent regression bugs
- Confidence untuk deploy frequently

#### 5. Separation of Concerns
```
Developer → Push code → CI/CD → Automated deployment
            (focus on code)     (handle infrastructure)
```

### Penerapan CI/CD pada Proyek Lain

#### 1. Web Application (Full-Stack)

```yaml
stages:
  - lint
  - test
  - build
  - deploy-staging
  - integration-test
  - deploy-production

# Example for React + Node.js
frontend-test:
  stage: test
  script:
    - cd frontend
    - npm install
    - npm run test
    - npm run build

backend-test:
  stage: test
  script:
    - cd backend
    - npm install
    - npm run test
    - npm run lint

deploy-staging:
  stage: deploy-staging
  script:
    - kubectl apply -f k8s/staging/
  environment:
    name: staging
    url: https://staging.myapp.com

deploy-production:
  stage: deploy-production
  script:
    - kubectl apply -f k8s/production/
  environment:
    name: production
    url: https://myapp.com
  when: manual  # Manual approval untuk production
  only:
    - main
```

#### 2. Mobile App (Flutter/React Native)

```yaml
stages:
  - test
  - build
  - distribute

test:
  script:
    - flutter test

build-android:
  script:
    - flutter build apk --release
  artifacts:
    paths:
      - build/app/outputs/apk/release/app-release.apk

build-ios:
  script:
    - flutter build ios --release

distribute:
  script:
    - firebase appdistribution:distribute build/app-release.apk \
        --app $FIREBASE_APP_ID \
        --groups "testers"
```

#### 3. Machine Learning Pipeline

```yaml
stages:
  - data-validation
  - training
  - model-evaluation
  - model-deployment

data-validation:
  script:
    - python validate_data.py
    - python check_data_drift.py

training:
  script:
    - python train_model.py
    - mlflow log-model model.pkl

model-evaluation:
  script:
    - python evaluate_model.py
    - if [ $ACCURACY -lt 0.85 ]; then exit 1; fi

model-deployment:
  script:
    - docker build -t ml-service:$CI_COMMIT_SHA .
    - kubectl set image deployment/ml-service ml=$IMAGE:$CI_COMMIT_SHA
```

#### 4. Microservices Architecture

```yaml
# .gitlab-ci.yml untuk setiap service
variables:
  SERVICE_NAME: user-service

stages:
  - test
  - build
  - deploy

test:
  script:
    - ./gradlew test
    - ./gradlew integrationTest

build:
  script:
    - docker build -t $CI_REGISTRY/$SERVICE_NAME:$CI_COMMIT_SHA .
    - docker push $CI_REGISTRY/$SERVICE_NAME:$CI_COMMIT_SHA

deploy:
  script:
    - kubectl set image deployment/$SERVICE_NAME \
        $SERVICE_NAME=$CI_REGISTRY/$SERVICE_NAME:$CI_COMMIT_SHA
    - kubectl rollout status deployment/$SERVICE_NAME
```

#### 5. Infrastructure as Code (Terraform)

```yaml
stages:
  - validate
  - plan
  - apply

terraform-validate:
  script:
    - terraform init
    - terraform validate
    - terraform fmt -check

terraform-plan:
  script:
    - terraform plan -out=tfplan
  artifacts:
    paths:
      - tfplan

terraform-apply:
  script:
    - terraform apply tfplan
  when: manual
  only:
    - main
```

### Best Practices untuk Proyek Lain

#### 1. Start Simple, Iterate
- Mulai dengan basic pipeline (build → test → deploy)
- Tambahkan stages secara bertahap

#### 2. Environment Strategy
```
Development → Staging → Production
(auto deploy)  (auto)    (manual approval)
```

#### 3. Security Integration
- Dependency scanning (npm audit, pip-audit)
- Container scanning (Trivy, Snyk)
- Secret management (GitLab CI/CD variables, Vault)

#### 4. Monitoring & Alerts
- Integrate dengan monitoring tools (Prometheus, Datadog)
- Send notification ke Slack/Discord saat pipeline fail
- Track deployment metrics (frequency, success rate, MTTR)

#### 5. Documentation
- Document pipeline di README.md
- Explain setiap stage dan requirement
- Troubleshooting guide untuk common issues

---