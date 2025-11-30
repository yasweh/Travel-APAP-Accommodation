<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<template>
  <div class="footer">
    <div class="footer-bg"></div>
    <div class="footer-content">
      <div class="footer-column">
        <div class="footer-brand">Travel Apap Accommodation</div>
        <div class="footer-description">The service at the Hotel Monteleone was exceptional. There was absolutely no issue that was not addressed timely and with satisfactory results.</div>
      </div>
      
      <div class="footer-column">
        <div class="footer-heading">Quick links</div>
        <div class="footer-link" @click="navigateTo('/booking/create')">Room booking</div>
        <div class="footer-link" @click="navigateTo('/room-type')">Rooms</div>
        <div class="footer-link" @click="navigateTo('/maintenance')">Maintenance</div>
        <div class="footer-link" @click="navigateTo('/property')">Properties</div>
      </div>
      
      <div class="footer-column">
        <div class="footer-heading">Company</div>
        <div class="footer-link">Privacy policy</div>
        <div class="footer-link">Refund policy</div>
        <div class="footer-link">F.A.Q</div>
        <div class="footer-link">About</div>
      </div>
      
      <div class="footer-column">
        <div class="footer-heading">Social media</div>
        <div class="footer-link">Facebook</div>
        <div class="footer-link">Twitter</div>
        <div class="footer-link">Instagram</div>
        <div class="footer-link">Linkedin</div>
      </div>
      
      <div class="footer-column">
        <div class="footer-heading">Newsletter</div>
        <div class="footer-newsletter-text">Kindly subscribe to our newsletter to get latest deals on our rooms and vacation discount.</div>
        <div class="newsletter-input">
          <div class="newsletter-input-bg"></div>
          <input type="email" placeholder="Enter your email" class="newsletter-email" />
          <div class="subscribe-btn">
            <div class="subscribe-bg"></div>
            <div class="subscribe-text">Subscribe</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Dancing+Script:wght@700&family=Raleway:wght@600;700&display=swap');

.footer {
  width: 100%;
  height: 420px;
  position: relative;
    height: auto;           /* ubah ke auto */
  padding-bottom: 80px;   /* tambahkan bottom spacing */
}

.footer-bg {
  width: 100%;
  height: 420px;
  background: #7C6A46;
  position: absolute;
  left: 0;
  top: 0;
}

.footer-content {
  position: relative;
  z-index: 1;
  width: calc(100% - 240px);
  max-width: 1272px;
  margin: 0 auto;
  padding-top: 81px;
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr 1.5fr;
  gap: 40px;
}

.footer-column {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.footer-brand {
  color: #FFF;
  font-family: 'Dancing Script', cursive;
  font-size: 30px;
  font-weight: 700;
}

.footer-description {
  color: #FFF;
  font-family: 'Raleway', sans-serif;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.6;
  text-align: justify;
}

.footer-heading {
  color: #FFF;
  font-family: 'Raleway', sans-serif;
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 3px;
}

.footer-link {
  color: #FFF;
  font-family: 'Raleway', sans-serif;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.3s ease;
}

.footer-link:hover {
  opacity: 0.8;
}

.footer-newsletter-text {
  color: #FFF;
  font-family: 'Raleway', sans-serif;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.5;
  text-align: justify;
}

.newsletter-input {
  width: 100%;
  height: 50px;
  position: relative;
}

.newsletter-input-bg {
  width: 100%;
  height: 50px;
  border-radius: 5px;
  background: #FFF;
  position: absolute;
}

.newsletter-email {
  width: calc(100% - 130px);
  height: 100%;
  border: none;
  outline: none;
  background: transparent;
  padding: 0 18px;
  color: #000;
  font-family: 'Raleway', sans-serif;
  font-size: 12px;
  position: relative;
  z-index: 1;
}

.newsletter-email::placeholder {
  color: rgba(0, 0, 0, 0.50);
}

.subscribe-btn {
  width: 111px;
  height: 40px;
  position: absolute;
  right: 5px;
  top: 5px;
  cursor: pointer;
  transition: transform 0.3s ease;
  z-index: 2;
}

.subscribe-btn:hover {
  transform: scale(1.05);
}

.subscribe-bg {
  width: 111px;
  height: 40px;
  border-radius: 2.5px;
  background: #7C6A46;
  position: absolute;
  filter: brightness(1.1);
}

.subscribe-text {
  color: #FFF;
  font-family: 'Raleway', sans-serif;
  font-size: 13px;
  font-weight: 600;
  position: absolute;
  left: 25px;
  top: 12px;
}

.footer-divider {
  width: 100%;
  height: 1px;
  background: rgba(217, 217, 217, 0.50);
  position: absolute;
  bottom: 118px;
}

.footer-copyright {
  color: #FFF;
  font-family: 'Raleway', sans-serif;
  font-size: 15px;
  font-weight: 600;
  text-align: center;
  position: absolute;
  bottom: 50px;
  width: 100%;
}

@media (max-width: 1400px) {
  .footer-content {
    width: calc(100% - 80px);
  }
}

@media (max-width: 1200px) {
  .footer-content {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr;
  }
  
  .footer {
    height: auto;
    min-height: 800px;
  }
  
  .footer-bg {
    height: 100%;
    min-height: 800px;
  }
}
</style>
