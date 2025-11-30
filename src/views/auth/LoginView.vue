<template>
  <div class="login-page">
    <!-- Left Column - Image -->
    <div class="login-left">
      <div class="left-overlay"></div>
      <img 
        class="left-image" 
        src="https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1200&q=80" 
        alt="Luxury Hotel" 
      />
      <div class="left-content">
        <div class="brand-logo">
          <svg width="56" height="32" viewBox="0 0 56 32" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M47.0332 24.8999H55.3332L27.6666 0L0 24.8999H8.29997L27.6666 7.44231L47.0332 24.8999ZM13.8333 5.00765V0H5.53332V12.45L13.8333 5.00765Z" fill="white"/>
            <path d="M25.8458 23.7379C25.8458 24.4462 25.6798 25.0936 25.3478 25.6801C25.0269 26.2556 24.5344 26.7204 23.8704 27.0745C23.2175 27.4176 22.4151 27.5891 21.4634 27.5891H19.8532V31.5399H16.5996V19.8369H21.4634C22.4041 19.8369 23.2009 20.0029 23.8538 20.3349C24.5178 20.6669 25.0158 21.1262 25.3478 21.7127C25.6798 22.2992 25.8458 22.9743 25.8458 23.7379ZM21.148 24.9995C22.0665 24.9995 22.5258 24.579 22.5258 23.7379C22.5258 22.8858 22.0665 22.4597 21.148 22.4597H19.8532V24.9995H21.148Z" fill="white"/>
            <path d="M38.5405 19.8369L34.5067 31.5399H30.3401L26.2897 19.8369H29.7757L32.4317 28.2863L35.0711 19.8369H38.5405Z" fill="white"/>
          </svg>
        </div>
        <h1 class="left-title">Travel Apap Accommodation</h1>
        <p class="left-subtitle">Hotel for every moment rich in emotion</p>
      </div>
    </div>

    <!-- Right Column - Login Form -->
    <div class="login-right">
      <div class="login-container">
        <div class="login-header">
          <h2>Welcome Back</h2>
          <p>Sign in to continue using Travel APAP</p>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label for="email">Email</label>
            <input
              id="email"
              v-model="email"
              type="email"
              placeholder="Enter your email"
              required
              :disabled="loading"
            />
          </div>

          <div class="form-group">
            <label for="password">Password</label>
            <input
              id="password"
              v-model="password"
              type="password"
              placeholder="Enter your password"
              required
              :disabled="loading"
            />
          </div>

          <div v-if="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>

          <button type="submit" class="login-button" :disabled="loading">
            {{ loading ? 'Logging in...' : 'Login' }}
          </button>
        </form>

        <div class="register-link">
          <p>Don't have an account? <a href="http://2306219575-fe.hafizmuh.site/register" target="_blank">Register</a></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authService } from '@/services/authService'

const router = useRouter()

const email = ref('')
const password = ref('')
const loading = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  if (!email.value || !password.value) {
    errorMessage.value = 'Please enter both email and password'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    await authService.login(email.value, password.value)
    
    // Redirect to home or intended page
    const redirectTo = router.currentRoute.value.query.redirect as string
    router.push(redirectTo || '/')
  } catch (error: any) {
    errorMessage.value = error.message || 'Login failed. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Raleway:wght@700;800&family=Poppins:wght@400;500;600&display=swap');

.login-page {
  display: flex;
  height: 100vh;
  min-height: 100vh;
  background: #fff;
  overflow: hidden;
}

/* Left Column - Image */
.login-left {
  position: relative;
  width: 50%;
  overflow: hidden;
}

.left-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  top: 0;
  left: 0;
}

.left-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(124, 106, 70, 0.85) 0%, rgba(90, 74, 48, 0.9) 100%);
  z-index: 1;
}

.left-content {
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px;
  text-align: center;
}

.brand-logo {
  margin-bottom: 30px;
  animation: fadeInDown 0.8s ease-out;
}

.left-title {
  font-family: 'Raleway', sans-serif;
  font-size: 48px;
  font-weight: 800;
  color: white;
  margin: 0 0 20px 0;
  letter-spacing: -1px;
  animation: fadeInUp 0.8s ease-out 0.2s both;
}

.left-subtitle {
  font-family: 'Poppins', sans-serif;
  font-size: 18px;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  line-height: 1.6;
  animation: fadeInUp 0.8s ease-out 0.4s both;
}

/* Right Column - Form */
.login-right {
  width: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #fafafa;
}

.login-container {
  width: 100%;
  max-width: 450px;
  animation: fadeIn 0.8s ease-out 0.3s both;
}

.login-header {
  margin-bottom: 40px;
}

.login-header h2 {
  font-family: 'Raleway', sans-serif;
  font-size: 32px;
  font-weight: 700;
  color: #1C1C1C;
  margin: 0 0 12px 0;
  letter-spacing: -0.5px;
}

.login-header p {
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  color: #666;
  margin: 0;
}

.login-form {
  background: white;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.form-group input {
  width: 100%;
  padding: 14px 16px;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  transition: all 0.3s ease;
  box-sizing: border-box;
  background: #fafafa;
}

.form-group input:focus {
  outline: none;
  border-color: #7C6A46;
  background: white;
  box-shadow: 0 0 0 4px rgba(124, 106, 70, 0.1);
}

.form-group input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message {
  background: #fee;
  color: #c33;
  padding: 14px 16px;
  border-radius: 10px;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  margin-bottom: 24px;
  border: 1px solid #fcc;
  display: flex;
  align-items: center;
  gap: 8px;
}

.login-button {
  width: 100%;
  padding: 16px;
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #7C6A46 0%, #5A4A30 100%);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(124, 106, 70, 0.3);
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(124, 106, 70, 0.4);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.register-link {
  margin-top: 24px;
  text-align: center;
}

.register-link p {
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  color: #666;
  margin: 0;
}

.register-link a {
  color: #7C6A46;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s ease;
}

.register-link a:hover {
  color: #5A4A30;
  text-decoration: underline;
}

/* Animations */
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* Responsive */
@media (max-width: 968px) {
  .login-page {
    flex-direction: column;
  }

  .login-left,
  .login-right {
    width: 100%;
  }

  .login-left {
    min-height: 300px;
  }

  .left-content {
    padding: 40px;
  }

  .left-title {
    font-size: 36px;
  }

  .left-subtitle {
    font-size: 16px;
  }

  .login-right {
    padding: 40px 20px;
  }

  .login-form {
    padding: 30px 24px;
  }
}

@media (max-width: 480px) {
  .left-title {
    font-size: 28px;
  }

  .left-subtitle {
    font-size: 14px;
  }

  .login-header h2 {
    font-size: 26px;
  }

  .login-header p {
    font-size: 14px;
  }

  .login-form {
    padding: 24px 20px;
  }
}
</style>
