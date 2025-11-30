<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { propertyService } from '@/services/propertyService'
import { bookingService } from '@/services/bookingService'

const router = useRouter()

const stats = ref({
  properties: 0,
  bookings: 0,
  roomTypes: 0
})

const loading = ref(true)

const loadStatistics = async () => {
  try {
    loading.value = true
    
    // Load properties count
    const propertiesResponse = await propertyService.getAll()
    if (propertiesResponse.success) {
      stats.value.properties = propertiesResponse.data.length
    }

    // Load bookings count
    try {
      const bookingsResponse = await bookingService.getAll()
      if (bookingsResponse.success) {
        stats.value.bookings = bookingsResponse.data.length
      }
    } catch (err) {
      // Ignore if booking service not ready
      stats.value.bookings = 0
    }

    // Estimate room types (rough calculation from properties)
    stats.value.roomTypes = stats.value.properties * 3 // Average 3 room types per property

  } catch (error) {
    console.error('Failed to load statistics:', error)
  } finally {
    loading.value = false
  }
}

const navigateTo = (path: string) => {
  router.push(path)
}

onMounted(() => {
  loadStatistics()
})
</script>

<template>
  <div class="homepage">
    <!-- Hero Section -->
    <div class="hero-bg"></div>
    <div class="hero-section">
      <div class="hero-content-left">
        <div class="brand-name">Travel Apap Accommodation</div>
        <div class="hero-title">Hotel for every moment rich in emotion</div>
        <div class="hero-subtitle">Every moment feels like the first time in Travel Apap Accommodation</div>
        <div class="hero-buttons">
          <div class="book-btn-hero" @click="navigateTo('/booking/create')">
            <div class="btn-bg-hero"></div>
            <div class="btn-text-hero">Book now</div>
          </div>
        </div>
      </div>
      <img class="hero-image" src="https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=80" alt="Hotel" />
    </div>

    <!-- Statistics Section with Real Data -->
    <div class="quick-booking">
      <div class="quick-booking-bg"></div>
      <div class="booking-controls">
        <div class="stat-item" @click="navigateTo('/property')">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M10 10C8.55 10 7.021 9.804 5.413 9.412C3.80433 9.02067 2 8.55 2 8C2 7.45 3.80433 6.979 5.413 6.587C7.021 6.19567 8.55 6 10 6C11.45 6 12.979 6.19567 14.587 6.587C16.196 6.979 18 7.45 18 8C18 8.55 16.196 9.02067 14.587 9.412C12.979 9.804 11.45 10 10 10ZM10 20C5.31667 17.7167 3.31267 15.5957 1.988 13.637C0.662667 11.679 0 9.86667 0 8.2C0 5.7 0.804333 3.70833 2.413 2.225C4.021 0.741667 5.88333 0 8 0C10.1167 0 11.979 0.741667 13.587 2.225C15.1957 3.70833 16 5.7 16 8.2C16 9.86667 15.3377 11.679 14.013 13.637C12.6877 15.5957 10.6833 17.7167 8 20Z" fill="#7C6A46"/>
          </svg>
          <div class="stat-label">Properties</div>
          <div class="stat-value">{{ loading ? '...' : stats.properties }}</div>
        </div>
        
        <div class="stat-item" @click="navigateTo('/booking')">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M0 17C0 18.7 1.3 20 3 20H17C18.7 20 20 18.7 20 17V9H0V17ZM17 2H15V1C15 0.4 14.6 0 14 0C13.4 0 13 0.4 13 1V2H7V1C7 0.4 6.6 0 6 0C5.4 0 5 0.4 5 1V2H3C1.3 2 0 3.3 0 5V7H20V5C20 3.3 18.7 2 17 2Z" fill="#7C6A46"/>
          </svg>
          <div class="stat-label">Bookings</div>
          <div class="stat-value">{{ loading ? '...' : stats.bookings }}</div>
        </div>
        
        <div class="stat-item" @click="navigateTo('/room-type')">
          <svg width="23" height="20" viewBox="0 0 23 20" fill="none">
            <path d="M16.6667 17.7778H18.8889V8.88889H12.2222V17.7778H14.4444V11.1111H16.6667V17.7778ZM1.11111 17.7778V1.11111C1.11111 0.816426 1.22817 0.533811 1.43655 0.325437C1.64492 0.117063 1.92754 0 2.22222 0H17.7778C18.0725 0 18.3551 0.117063 18.5635 0.325437C18.7718 0.533811 18.8889 0.816426 18.8889 1.11111V6.66667H21.1111V17.7778H22.2222V20H0V17.7778H1.11111ZM5.55556 8.88889V11.1111H7.77778V8.88889H5.55556ZM5.55556 13.3333V15.5556H7.77778V13.3333H5.55556ZM5.55556 4.44444V6.66667H7.77778V4.44444H5.55556Z" fill="#7C6A46"/>
          </svg>
          <div class="stat-label">Room Types</div>
          <div class="stat-value">{{ loading ? '...' : stats.roomTypes }}</div>
        </div>

        <div class="book-now-btn" @click="navigateTo('/booking/create')">
          <div class="book-now-bg"></div>
          <div class="book-now-text">Book Now</div>
        </div>
      </div>
    </div>

    <!-- Facilities Section -->
    <div class="facilities-title">Our Facilities</div>
    <div class="facilities-subtitle">We offer modern (5 star) hotel facilities for your comfort.</div>
    
    <div class="facilities-row">
      <div class="facility-card" @click="navigateTo('/property')">
        <div class="facility-bg"></div>
        <div class="facility-content">
          <svg width="55" height="40" viewBox="0 0 55 40" fill="none">
            <path d="M36.7673 0C36.36 0 35.1855 0.534546 35.1855 0.534546L23.1055 6.64364C21.4945 7.28 20.8582 9.84727 21.8255 11.1309L25.3527 16.2509L10.9164 23.6291L18.1818 29.0836L27.28 23.6291L36.3709 29.0836L40.0145 25.44L29.1055 10.8945L38.4036 5.33091C40.3236 4.36364 40.0145 2.77091 40.0145 1.80364C40 1.04 38.7055 0 36.7673 0ZM44.5527 10.9091C43.7168 10.9084 42.8889 11.0723 42.1164 11.3915C41.3438 11.7108 40.6417 12.1791 40.0501 12.7696C39.4585 13.3602 38.989 14.0615 38.6684 14.8336C38.3479 15.6056 38.1825 16.4332 38.1818 17.2691C38.1811 18.105 38.345 18.9329 38.6643 19.7055C38.9835 20.478 39.4518 21.1802 40.0424 21.7717C40.6329 22.3633 41.3343 22.8328 42.1063 23.1534C42.8783 23.4739 43.7059 23.6393 44.5418 23.64C46.23 23.6414 47.8497 22.9722 49.0445 21.7795C50.2393 20.5867 50.9113 18.9682 50.9127 17.28C50.9142 15.5918 50.2449 13.9721 49.0522 12.7773C47.8595 11.5826 46.241 10.9105 44.5527 10.9091ZM9.09091 29.0909L0 34.5455V40L9.09091 34.5455L18.1818 40L27.28 34.5455L36.3709 40L43.6364 34.5455L54.5455 40V34.5455L43.6364 29.0909L36.3709 34.5455L27.28 29.0909L18.1818 34.5455L9.09091 29.0909Z" fill="#7C6A46"/>
          </svg>
          <div class="facility-name">Swimming Pool</div>
        </div>
      </div>

      <div class="facility-card">
        <div class="facility-bg"></div>
        <div class="facility-content">
          <svg width="56" height="40" viewBox="0 0 57 40" fill="none">
            <path d="M14.9412 26.7059L10 21.6471C12.3137 19.3333 15.0298 17.5004 18.1482 16.1482C21.2667 14.7961 24.629 14.1192 28.2353 14.1176C31.8431 14.1176 35.2063 14.8039 38.3247 16.1765C41.4431 17.549 44.1584 19.4118 46.4706 21.7647L41.5294 26.7059C39.8039 24.9804 37.8039 23.6275 35.5294 22.6471C33.2549 21.6667 30.8235 21.1765 28.2353 21.1765C25.6471 21.1765 23.2157 21.6667 20.9412 22.6471C18.6667 23.6275 16.6667 24.9804 14.9412 26.7059ZM4.94118 16.7059L0 11.7647C3.60784 8.07843 7.82353 5.19608 12.6471 3.11765C17.4706 1.03922 22.6667 0 28.2353 0C33.8039 0 39 1.03922 43.8235 3.11765C48.6471 5.19608 52.8627 8.07843 56.4706 11.7647L51.5294 16.7059C48.5098 13.6863 45.0094 11.3231 41.0282 9.61647C37.0471 7.9098 32.7827 7.05726 28.2353 7.05882C23.6863 7.05882 19.4212 7.91216 15.44 9.61882C11.4588 11.3255 7.95922 13.6878 4.94118 16.7059ZM28.2353 40L36.5294 31.6471C35.4706 30.5882 34.2353 29.7545 32.8235 29.1459C31.4118 28.5373 29.8824 28.2337 28.2353 28.2353C26.5882 28.2353 25.0588 28.5396 23.6471 29.1482C22.2353 29.7569 21 30.5898 19.9412 31.6471L28.2353 40Z" fill="#7C6A46"/>
          </svg>
          <div class="facility-name">Wifi</div>
        </div>
      </div>

      <div class="facility-card">
        <div class="facility-bg"></div>
        <div class="facility-content">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <path d="M7.23684 12.5474L8.33158 11.4316C9.55263 10.2105 11.5737 10.2105 12.8368 11.5158L12.8789 11.5789C13.9105 12.6316 15.2789 13.1368 16.7737 13.2842C18.8158 13.4737 20.7947 14.5684 22.1 16.5474C23.5316 18.8211 23.5105 21.8526 22.0158 24.1053C21.452 24.9968 20.6939 25.7493 19.7981 26.3063C18.9023 26.8634 17.8922 27.2106 16.8432 27.322C15.7943 27.4333 14.7337 27.306 13.741 26.9495C12.7482 26.5929 11.8489 26.0164 11.1105 25.2632C9.93158 24.1053 9.2579 22.6105 9.08947 21.0526C8.92105 19.5158 8.31053 18.0632 7.23684 16.9895C6.01579 15.7895 6.01579 13.7684 7.23684 12.5474ZM16.0789 29.4737C13.6158 29.4737 11.3211 28.5263 9.61579 26.7789C8.14211 25.2632 7.21579 23.3684 6.98421 21.3053C6.92105 20.6737 6.73158 19.8526 6.22632 19.0947C4.96316 20.8421 4.20526 22.9474 4.20526 25.2632C4.20526 28.7158 5.88947 31.7684 8.47895 33.6842H35.7842V31.5789C35.7842 24 30.7526 17.5789 23.8684 15.4737C25.7421 18.4211 25.7 22.3579 23.7842 25.2632C22.1 27.8947 19.1947 29.4737 16.0789 29.4737ZM26.9 11.7053H29.5737C31.4053 11.7053 33.0053 12.9895 33.0053 15.2842V16.8421H35.6368V14.7368C35.6368 11.5789 32.8368 9.17895 29.5737 9.17895H26.9C25.1526 9.17895 23.6579 7.45263 23.6579 5.68421C23.6579 3.91579 25.1526 2.61053 26.9 2.61053V0C23.6579 0 21.0474 2.61053 21.0474 5.85263C21.0474 9.09474 23.6579 11.7053 26.9 11.7053ZM5.25789 11.6842C5.38421 11.4737 5.55263 11.2632 5.74211 11.0526L6.83684 9.9579C7.02632 9.76842 7.23684 9.62105 7.44737 9.47368L4.47895 6.46316C4.77368 5.89474 4.66842 5.15789 4.20526 4.67368C3.90379 4.38022 3.49967 4.21601 3.07895 4.21601C2.65822 4.21601 2.25411 4.38022 1.95263 4.67368C1.65789 4.96842 1.51053 5.32632 1.48947 5.70526C1.11053 5.72632 0.752632 5.87368 0.457895 6.16842C-0.152632 6.77895 -0.152632 7.78947 0.457895 8.42105C0.942105 8.88421 1.65789 8.98947 2.24737 8.69474L5.25789 11.6842ZM34.5 5.03158C35.5737 3.95789 36.2474 2.50526 36.2474 0.884211H33.6158C33.6158 2.63158 32.1421 4.10526 30.3947 4.10526V6.71579C34.3105 6.71579 37.3842 9.91579 37.3842 13.8316V18.9474H39.9947V13.8316C40.0053 11.9952 39.4945 10.1935 38.5219 8.63576C37.5493 7.07805 36.1546 5.82827 34.5 5.03158ZM6.31053 40H35.7842C38.1211 40 39.9947 38.1263 39.9947 35.7895H2.1C2.1 36.9062 2.54361 37.9771 3.33323 38.7668C4.12286 39.5564 5.19383 40 6.31053 40Z" fill="#7C6A46"/>
          </svg>
          <div class="facility-name">Breakfast</div>
        </div>
      </div>

      <div class="facility-card">
        <div class="facility-bg"></div>
        <div class="facility-content">
          <svg width="67" height="40" viewBox="0 0 67 40" fill="none">
            <path d="M11.6667 3.33333C11.6667 1.48958 13.1562 0 15 0H16.6667C18.5104 0 20 1.48958 20 3.33333V36.6667C20 38.5104 18.5104 40 16.6667 40H15C13.1562 40 11.6667 38.5104 11.6667 36.6667V33.3333H6.66667C4.82292 33.3333 3.33333 31.8438 3.33333 30V23.3333C1.48958 23.3333 0 21.8438 0 20C0 18.1562 1.48958 16.6667 3.33333 16.6667V10C3.33333 8.15625 4.82292 6.66667 6.66667 6.66667H11.6667V3.33333ZM55 3.33333V6.66667H60C61.8438 6.66667 63.3333 8.15625 63.3333 10V16.6667C65.1771 16.6667 66.6667 18.1562 66.6667 20C66.6667 21.8438 65.1771 23.3333 63.3333 23.3333V30C63.3333 31.8438 61.8438 33.3333 60 33.3333H55V36.6667C55 38.5104 53.5104 40 51.6667 40H50C48.1562 40 46.6667 38.5104 46.6667 36.6667V3.33333C46.6667 1.48958 48.1562 0 50 0H51.6667C53.5104 0 55 1.48958 55 3.33333ZM43.3333 16.6667V23.3333H23.3333V16.6667H43.3333Z" fill="#7C6A46"/>
          </svg>
          <div class="facility-name">Gym</div>
        </div>
      </div>
    </div>

    <!-- Additional Facilities -->
    <div class="facilities-row">
      <div class="facility-card">
        <div class="facility-bg"></div>
        <div class="facility-content">
          <svg width="55" height="40" viewBox="0 0 55 40" fill="none">
            <path d="M53.0713 18.7906C50.6615 7.89557 47.0347 2.08051 41.6548 0.492105C40.5239 0.16039 39.3509 -0.00533123 38.1724 0.000130728C36.6146 0.000130728 35.258 0.379621 33.823 0.781835C32.0937 1.26699 30.1292 1.81805 27.2649 1.81805C24.4005 1.81805 22.4349 1.26813 20.7022 0.782972C19.266 0.379621 17.9105 0.000130728 16.3573 0.000130728C15.1387 -0.00409489 13.9254 0.161098 12.7522 0.490968C7.4007 2.07256 3.77622 7.88535 1.33112 18.7838C-1.29804 30.5116 -0.00390906 37.9162 4.96014 39.6341C5.64059 39.874 6.35654 39.9977 7.07802 40C10.4787 40 13.2055 37.1675 15.0689 34.8485C17.1743 32.2239 19.6387 30.8922 27.2649 30.8922C34.0764 30.8922 36.8953 31.816 39.3302 34.8485C40.8606 36.755 42.307 38.0889 43.75 38.9286C45.669 40.0443 47.5869 40.292 49.4491 39.6523C52.3828 38.6513 54.0644 36.0051 54.4484 31.7853C54.7404 28.5494 54.2905 24.2989 53.0713 18.7906ZM21.8111 18.1793H18.1753V21.8151C18.1753 22.2973 17.9837 22.7597 17.6428 23.1006C17.3019 23.4415 16.8395 23.6331 16.3573 23.6331C15.8752 23.6331 15.4128 23.4415 15.0719 23.1006C14.731 22.7597 14.5394 22.2973 14.5394 21.8151V18.1793H10.9036C10.4215 18.1793 9.95906 17.9878 9.61814 17.6469C9.27721 17.3059 9.08568 16.8435 9.08568 16.3614C9.08568 15.8792 9.27721 15.4169 9.61814 15.0759C9.95906 14.735 10.4215 14.5435 10.9036 14.5435H14.5394V10.9076C14.5394 10.4255 14.731 9.9631 15.0719 9.62218C15.4128 9.28125 15.8752 9.08972 16.3573 9.08972C16.8395 9.08972 17.3019 9.28125 17.6428 9.62218C17.9837 9.9631 18.1753 10.4255 18.1753 10.9076V14.5435H21.8111C22.2932 14.5435 22.7556 14.735 23.0966 15.0759C23.4375 15.4169 23.629 15.8792 23.629 16.3614C23.629 16.8435 23.4375 17.3059 23.0966 17.6469C22.7556 17.9878 22.2932 18.1793 21.8111 18.1793ZM31.3552 18.6338C30.9057 18.6338 30.4664 18.5005 30.0927 18.2508C29.719 18.0011 29.4277 17.6462 29.2558 17.231C29.0838 16.8158 29.0388 16.3589 29.1264 15.9181C29.2141 15.4773 29.4305 15.0724 29.7483 14.7546C30.0661 14.4368 30.4711 14.2203 30.9119 14.1327C31.3527 14.045 31.8096 14.09 32.2248 14.262C32.64 14.434 32.9949 14.7252 33.2446 15.0989C33.4943 15.4726 33.6276 15.912 33.6276 16.3614C33.6276 16.9641 33.3882 17.5421 32.962 17.9682C32.5358 18.3944 31.9578 18.6338 31.3552 18.6338ZM36.3544 23.6331C35.9048 23.6331 35.4652 23.4996 35.0914 23.2497C34.7176 22.9997 34.4263 22.6445 34.2545 22.229C34.0827 21.8134 34.038 21.3562 34.1261 20.9152C34.2143 20.4743 34.4313 20.0694 34.7496 19.7518C35.068 19.4343 35.4734 19.2183 35.9146 19.1312C36.3558 19.0442 36.8129 19.09 37.228 19.2629C37.6431 19.4357 37.9976 19.7279 38.2466 20.1023C38.4956 20.4768 38.628 20.9167 38.6268 21.3663C38.6253 21.968 38.3853 22.5446 37.9593 22.9695C37.5333 23.3944 36.9561 23.6331 36.3544 23.6331ZM36.3544 13.6345C35.905 13.6345 35.4657 13.5012 35.092 13.2515C34.7183 13.0019 34.427 12.647 34.255 12.2317C34.083 11.8165 34.038 11.3596 34.1257 10.9188C34.2134 10.478 34.4298 10.0731 34.7476 9.75529C35.0654 9.43749 35.4703 9.22106 35.9111 9.13338C36.3519 9.0457 36.8088 9.0907 37.2241 9.2627C37.6393 9.43469 37.9942 9.72595 38.2439 10.0996C38.4936 10.4733 38.6268 10.9127 38.6268 11.3621C38.6268 11.9648 38.3874 12.5428 37.9613 12.9689C37.5351 13.3951 36.9571 13.6345 36.3544 13.6345ZM41.3537 18.6338C40.9043 18.6338 40.4649 18.5005 40.0912 18.2508C39.7175 18.0011 39.4263 17.6462 39.2543 17.231C39.0823 16.8158 39.0373 16.3589 39.125 15.9181C39.2127 15.4773 39.4291 15.0724 39.7469 14.7546C40.0647 14.4368 40.4696 14.2203 40.9104 14.1327C41.3512 14.045 41.8081 14.09 42.2233 14.262C42.6386 14.434 42.9935 14.7252 43.2431 15.0989C43.4928 15.4726 43.6261 15.912 43.6261 16.3614C43.6261 16.9641 43.3867 17.5421 42.9605 17.9682C42.5344 18.3944 41.9564 18.6338 41.3537 18.6338Z" fill="#7C6A46"/>
          </svg>
          <div class="facility-name">Game center</div>
        </div>
      </div>

      <div class="facility-card">
        <div class="facility-bg"></div>
        <div class="facility-content">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <path d="M20 9.09091C22.8933 9.09091 25.668 10.2403 27.7139 12.2861C29.7597 14.332 30.9091 17.1067 30.9091 20C30.9091 24.0364 28.7091 27.5636 25.4545 29.4545V32.7273C25.4545 33.2095 25.263 33.6719 24.922 34.0129C24.581 34.3539 24.1186 34.5455 23.6364 34.5455H16.3636C15.8814 34.5455 15.419 34.3539 15.078 34.0129C14.737 33.6719 14.5455 33.2095 14.5455 32.7273V29.4545C11.2909 27.5636 9.09091 24.0364 9.09091 20C9.09091 17.1067 10.2403 14.332 12.2861 12.2861C14.332 10.2403 17.1067 9.09091 20 9.09091ZM23.6364 36.3636V38.1818C23.6364 38.664 23.4448 39.1265 23.1038 39.4675C22.7629 39.8084 22.3004 40 21.8182 40H18.1818C17.6996 40 17.2371 39.8084 16.8962 39.4675C16.5552 39.1265 16.3636 38.664 16.3636 38.1818V36.3636H23.6364ZM34.5455 18.1818H40V21.8182H34.5455V18.1818ZM0 18.1818H5.45455V21.8182H0V18.1818ZM21.8182 0V5.45455H18.1818V0H21.8182ZM7.12727 4.54545L11 8.43636L8.41818 11L4.54545 7.14545L7.12727 4.54545ZM29 8.41818L32.8545 4.54545L35.4545 7.14545L31.5818 11L29 8.41818Z" fill="#7C6A46"/>
          </svg>
          <div class="facility-name">24/7 Light</div>
        </div>
      </div>

      <div class="facility-card">
        <div class="facility-bg"></div>
        <div class="facility-content">
          <svg width="32" height="40" viewBox="0 0 32 40" fill="none">
            <path d="M11.28 28.72C13.88 31.32 18.12 31.32 20.72 28.72C23.32 26.12 23.32 21.88 20.72 19.28L11.28 28.72ZM28 0.02L4 0C1.78 0 0 1.78 0 4V36C0 38.22 1.78 40 4 40H28C30.22 40 32 38.22 32 36V4C32 1.78 30.22 0.02 28 0.02ZM14 6C15.1 6 16 6.9 16 8C16 9.1 15.1 10 14 10C12.9 10 12 9.1 12 8C12 6.9 12.9 6 14 6ZM8 6C9.1 6 10 6.9 10 8C10 9.1 9.1 10 8 10C6.9 10 6 9.1 6 8C6 6.9 6.9 6 8 6ZM16 34C10.48 34 6 29.52 6 24C6 18.48 10.48 14 16 14C21.52 14 26 18.48 26 24C26 29.52 21.52 34 16 34Z" fill="#7C6A46"/>
          </svg>
          <div class="facility-name">Laundry</div>
        </div>
      </div>

      <div class="facility-card">
        <div class="facility-bg"></div>
        <div class="facility-content">
          <svg width="42" height="40" viewBox="0 0 42 40" fill="none">
            <path d="M29.5236 28.6052C27.5642 30.0186 25.1746 30.7074 22.7635 30.5535H17.1546V40H11.5458V13.4317H23.1177C25.4205 13.3096 27.6885 14.0341 29.4941 15.4686C30.3366 16.2844 30.9879 17.2766 31.4013 18.3739C31.8148 19.4713 31.98 20.6467 31.8852 21.8155C32.0371 23.0525 31.9031 24.3078 31.4937 25.4849C31.0843 26.662 30.4103 27.7294 29.5236 28.6052ZM25.0956 19.0406C24.2425 18.4151 23.2001 18.1024 22.1435 18.155H17.1546V25.9779H22.1435C23.2099 26.0343 24.2601 25.6983 25.0956 25.0332C25.4691 24.6427 25.7567 24.1783 25.9398 23.6698C26.1228 23.1614 26.1973 22.6202 26.1583 22.0812C26.227 21.5268 26.1669 20.964 25.9825 20.4366C25.7982 19.9092 25.4947 19.4315 25.0956 19.0406ZM41.066 11.0996C41.2277 10.7469 41.244 10.3448 41.1114 9.98014C40.9789 9.61551 40.708 9.31775 40.3576 9.15129L20.6675 0L0.859406 9.15129C0.683018 9.2327 0.524392 9.34806 0.392585 9.49077C0.260778 9.63348 0.158371 9.80076 0.0912112 9.98305C0.0240514 10.1653 -0.00654594 10.3591 0.00116619 10.5532C0.00887832 10.7473 0.0547489 10.938 0.136159 11.1144C0.217569 11.2908 0.332923 11.4494 0.475637 11.5812C0.618351 11.713 0.785629 11.8154 0.967919 11.8826C1.15021 11.9497 1.34394 11.9803 1.53806 11.9726C1.73217 11.9649 1.92287 11.919 2.09926 11.8376L20.6675 3.24723L39.2358 11.8081C39.5885 11.9698 39.9907 11.9861 40.3553 11.8535C40.7199 11.7209 41.0177 11.4501 41.1841 11.0996H41.066Z" fill="#7C6A46"/>
          </svg>
          <div class="facility-name">Parking space</div>
        </div>
      </div>
    </div>

    <!-- Rooms Section -->
    <div class="rooms-bg">
      <div class="rooms-filter"></div>
      <div class="rooms-section">
        <div class="rooms-title">Luxurious Rooms</div>
        <div class="rooms-subtitle">All room are design for your comfort</div>
        <div class="rooms-divider"></div>
        
        <div class="rooms-grid">
          <div class="room-card" @click="navigateTo('/room-type')">
            <div class="room-card-bg"></div>
            <img class="room-image" src="https://images.unsplash.com/photo-1590490360182-c33d57733427?w=400&q=80" alt="Standard Room" />
            <div class="avail-btn">
              <div class="avail-bg"></div>
              <div class="avail-text">2 Rooms available</div>
            </div>
            <div class="room-description">Television set, Extra sheets and Breakfast</div>
          </div>

          <div class="room-card" @click="navigateTo('/room-type')">
            <div class="room-card-bg"></div>
            <img class="room-image" src="https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=400&q=80" alt="Deluxe Room" />
            <div class="avail-btn">
              <div class="avail-bg"></div>
              <div class="avail-text">4 Rooms available</div>
            </div>
            <div class="room-description">Television set, Extra sheets, Breakfast, and fireplace</div>
          </div>

          <div class="room-card" @click="navigateTo('/room-type')">
            <div class="room-card-bg"></div>
            <img class="room-image" src="https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=400&q=80" alt="Suite Room" />
            <div class="avail-btn">
              <div class="avail-bg"></div>
              <div class="avail-text">8 Rooms available</div>
            </div>
            <div class="room-description">Television set, Extra sheets, Breakfast, and fireplace, Console and bed rest</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Testimonials Section -->
    <div class="testimonials-section">
      <div class="testimonials-title">Testimonies</div>
      <div class="testimonials-grid">
        <div class="testimonial-card">
          <div class="testimonial-rating">⭐⭐⭐⭐⭐</div>
          <p class="testimonial-text">"The service at Travel Apap Accommodation was exceptional. The staff anticipated our needs and the rooms were incredibly comfortable."</p>
          <div class="testimonial-author">- John Smith</div>
        </div>
        <div class="testimonial-card">
          <div class="testimonial-rating">⭐⭐⭐⭐⭐</div>
          <p class="testimonial-text">"Amazing facilities and beautiful views. The swimming pool and gym were top-notch. Highly recommended!"</p>
          <div class="testimonial-author">- Sarah Johnson</div>
        </div>
        <div class="testimonial-card">
          <div class="testimonial-rating">⭐⭐⭐⭐⭐</div>
          <p class="testimonial-text">"Perfect for a relaxing vacation. The breakfast was delicious and the rooms were spotless. Will definitely return!"</p>
          <div class="testimonial-author">- Michael Brown</div>
        </div>
      </div>
    </div>
    </div>
</template>


<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Dancing+Script:wght@600;700&family=Poppins:wght@500;700&family=Raleway:wght@400;500;600;700&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.homepage {
  width: 100%;
  min-height: 100vh;
  background: #FFF;
  position: relative;
}

/* Navbar */
.navbar {
  width: 100%;
  height: 120px;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;
}

.navbar-bg {
  width: 100%;
  height: 120px;
  background: #FAFAFA;
  position: absolute;
  left: 0;
  top: 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.nav-links {
  width: calc(100% - 240px);
  max-width: 1271px;
  height: 55px;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  top: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  width: 55px;
  height: 32px;
  cursor: pointer;
}

.links {
  display: flex;
  gap: 60px;
  align-items: center;
}

.link {
  color: #000;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px 0;
}

.link.active {
  color: #7C6A46;
  font-weight: 700;
  border-bottom: 2px solid #7C6A46;
}

.link:hover {
  color: #7C6A46;
}

.book-btn {
  width: 165px;
  height: 55px;
  position: relative;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.book-btn:hover {
  transform: translateY(-2px);
}

.btn-bg {
  width: 165px;
  height: 55px;
  border-radius: 5px;
  background: #7C6A46;
  position: absolute;
}

.btn-text {
  color: #FFF;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 500;
  position: absolute;
  left: 46px;
  top: 16px;
}

/* Hero Section */
.hero-bg {
  width: 100%;
  height: 750px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F0EDE6 100%);
  position: absolute;
  left: 0;
  top: 120px;
}

.hero-section {
  width: calc(100% - 240px);
  max-width: 1272px;
  margin: 0 auto;
  padding-top: 80px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 80px;
  position: relative;
}

.hero-content-left {
  flex: 1;
  max-width: 580px;
  z-index: 2;
  animation: fadeInLeft 1s ease-out;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.brand-name {
  color: #7C6A46;
  font-family: 'Dancing Script', cursive;
  font-size: 60px;
  font-weight: 700;
  margin-bottom: 20px;
  text-shadow: 0 2px 4px rgba(124, 106, 70, 0.1);
  animation: fadeIn 1.2s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.hero-title {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 65px;
  font-weight: 800;
  line-height: 1.15;
  margin-bottom: 28px;
  letter-spacing: -0.5px;
}

.hero-subtitle {
  color: #4A4A4A;
  font-family: 'Raleway', sans-serif;
  font-size: 18px;
  font-weight: 500;
  line-height: 1.6;
  margin-bottom: 45px;
  opacity: 0.9;
}

.hero-buttons {
  display: flex;
  gap: 38px;
  align-items: center;
}

.book-btn-hero {
  width: 190px;
  height: 60px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
  filter: drop-shadow(0 4px 12px rgba(124, 106, 70, 0.25));
}

.book-btn-hero:hover {
  transform: translateY(-3px);
  filter: drop-shadow(0 6px 16px rgba(124, 106, 70, 0.35));
}

.btn-bg-hero {
  width: 190px;
  height: 60px;
  border-radius: 8px;
  background: linear-gradient(135deg, #7C6A46 0%, #9B8A68 100%);
  position: absolute;
  box-shadow: 0 4px 15px rgba(124, 106, 70, 0.3);
}

.btn-text-hero {
  color: #FFF;
  font-family: 'Poppins', sans-serif;
  font-size: 17px;
  font-weight: 600;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  letter-spacing: 0.5px;
}

.hero-image {
  width: 660px;
  height: 750px;
  object-fit: cover;
  border-radius: 20px;
  flex-shrink: 0;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  position: relative;
  animation: fadeInRight 1s ease-out;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Quick Booking / Statistics Section */
.quick-booking {
  width: calc(100% - 240px);
  max-width: 1272px;
  height: 120px;
  position: relative;
  margin: 80px auto;
  cursor: default;
}

.quick-booking-bg {
  width: 100%;
  height: 120px;
  background: #FFF;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  position: absolute;
  left: 0;
  top: 0;
}

.booking-controls {
  width: calc(100% - 80px);
  max-width: 1195px;
  height: 65px;
  position: absolute;
  left: 40px;
  top: 27px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 30px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  cursor: pointer;
  transition: transform 0.3s ease;
  padding: 10px 20px;
  border-radius: 8px;
}

.stat-item:hover {
  background: #FAFAFA;
  transform: translateY(-2px);
}

.stat-item svg {
  width: 20px;
  height: 20px;
  margin-bottom: 5px;
}

.stat-label {
  color: #1C1C1C;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 500;
}

.stat-value {
  color: rgba(28, 28, 28, 0.60);
  font-family: 'Poppins', sans-serif;
  font-size: 20px;
  font-weight: 700;
}

.book-now-btn {
  width: 180px;
  height: 65px;
  position: relative;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.book-now-btn:hover {
  transform: scale(1.05);
}

.book-now-bg {
  width: 180px;
  height: 65px;
  border-radius: 5px;
  background: #7C6A46;
  position: absolute;
}

.book-now-text {
  color: #FFF;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 500;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

/* Facilities Section */
.facilities-title {
  color: #000;
  font-family: 'Poppins', sans-serif;
  font-size: 40px;
  font-weight: 500;
  text-align: center;
  margin-bottom: 12px;
}

.facilities-subtitle {
  color: #000;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 500;
  text-align: center;
  margin-bottom: 50px;
}

.facilities-row {
  width: calc(100% - 240px);
  max-width: 1273px;
  margin: 0 auto 40px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 51px;
}

.facility-card {
  width: 100%;
  height: 250px;
  position: relative;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.facility-card:hover {
  transform: translateY(-10px);
}

.facility-bg {
  width: 100%;
  height: 250px;
  border-radius: 5px;
  background: #FAFAFA;
  position: absolute;
  left: 0;
  top: 0;
}

.facility-content {
  position: absolute;
  width: 100%;
  height: 93px;
  left: 50%;
  transform: translateX(-50%);
  bottom: 85px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 23px;
}

.facility-content svg {
  width: 55px;
  height: 40px;
}

.facility-name {
  color: #7C6A46;
  font-family: 'Poppins', sans-serif;
  font-size: 20px;
  font-weight: 500;
  text-align: center;
}

/* Rooms Section */
.rooms-bg {
  width: 100%;
  height: 650px;
  background-image: url('https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=1600&q=80');
  background-size: cover;
  background-position: center;
  position: relative;
  margin-top: 60px;
}

.rooms-filter {
  width: 100%;
  height: 650px;
  background: rgba(124, 106, 70, 0.80);
  position: absolute;
  left: 0;
  top: 0;
}

.rooms-section {
  position: relative;
  z-index: 1;
  padding-top: 27px;
}

.rooms-title {
  color: #FFF;
  font-family: 'Raleway', sans-serif;
  font-size: 50px;
  font-weight: 500;
  text-align: center;
  margin-bottom: 15px;
}

.rooms-subtitle {
  color: #FFF;
  font-family: 'Raleway', sans-serif;
  font-size: 15px;
  font-weight: 500;
  text-align: center;
  margin-bottom: 14px;
}

.rooms-divider {
  width: 138px;
  height: 6px;
  background: #FFF;
  margin: 0 auto 43px;
}

.rooms-grid {
  width: calc(100% - 240px);
  max-width: 1271px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 58px;
}

.room-card {
  width: 100%;
  height: 384px;
  position: relative;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.room-card:hover {
  transform: translateY(-10px);
}

.room-card-bg {
  width: 100%;
  height: 384px;
  border-radius: 10px;
  background: #FFF;
  position: absolute;
  left: 0;
  top: 0;
}

.room-image {
  width: calc(100% - 54px);
  height: 285px;
  border-radius: 5px;
  object-fit: cover;
  position: absolute;
  left: 27px;
  top: 27px;
}

.avail-btn {
  width: 107px;
  height: 34px;
  position: absolute;
  right: 40px;
  top: 44px;
  z-index: 1;
}

.avail-bg {
  width: 107px;
  height: 34px;
  border-radius: 2.5px;
  background: #7C6A46;
  position: absolute;
}

.avail-text {
  color: #FFF;
  font-family: 'Raleway', sans-serif;
  font-size: 11px;
  font-weight: 700;
  position: absolute;
  left: 6px;
  top: 8px;
}

.room-description {
  color: #000;
  font-family: 'Raleway', sans-serif;
  font-size: 17px;
  font-weight: 400;
  position: absolute;
  left: 27px;
  bottom: 16px;
  right: 27px;
  line-height: 1.3;
}

/* Auth Container */
.auth-container {
  position: fixed;
  top: 20px;
  right: 40px;
  z-index: 1001;
  display: flex;
  align-items: center;
  gap: 15px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 12px 20px;
  border-radius: 50px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7C6A46 0%, #9B8A68 100%);
  color: #FFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Poppins', sans-serif;
  font-size: 18px;
  font-weight: 600;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  color: #1C1C1C;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 600;
}

.user-role {
  color: #7C6A46;
  font-family: 'Poppins', sans-serif;
  font-size: 12px;
  font-weight: 500;
}

.logout-btn,
.login-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 25px;
  background: linear-gradient(135deg, #7C6A46 0%, #9B8A68 100%);
  color: #FFF;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(124, 106, 70, 0.2);
}

.logout-btn:hover,
.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(124, 106, 70, 0.3);
}

.logout-btn svg {
  width: 18px;
  height: 18px;
}

/* Testimonials Section */
.testimonials-section {
  width: calc(100% - 240px);
  max-width: 1273px;
  margin: 80px auto;
}

.testimonials-title {
  color: #000;
  font-family: 'Raleway', sans-serif;
  font-size: 50px;
  font-weight: 500;
  text-align: center;
  margin-bottom: 50px;
}

.testimonials-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 39px;
}

.testimonial-card {
  background: #FAFAFA;
  border-radius: 10px;
  padding: 40px 30px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.testimonial-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.testimonial-rating {
  font-size: 24px;
  margin-bottom: 20px;
}

.testimonial-text {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 15px;
  font-weight: 400;
  line-height: 1.6;
  margin-bottom: 20px;
}

.testimonial-author {
  color: #7C6A46;
  font-family: 'Raleway', sans-serif;
  font-size: 14px;
  font-weight: 600;
  text-align: right;
}

/* Footer */
.footer {
  width: 100%;
  height: 420px;
  position: relative;
  margin-top: 80px;
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

/* Responsive Design */
@media (max-width: 1400px) {
  .nav-links,
  .hero-section,
  .quick-booking,
  .facilities-row,
  .rooms-grid,
  .testimonials-section,
  .footer-content {
    width: calc(100% - 80px);
  }
}

@media (max-width: 1200px) {
  .hero-section {
    flex-direction: column;
    text-align: center;
    padding-top: 100px;
  }
  
  .hero-content-left {
    max-width: 100%;
  }
  
  .hero-image {
    width: 100%;
    max-width: 660px;
    height: 550px;
  }
  
  .hero-buttons {
    justify-content: center;
  }
  
  .hero-bg {
    height: auto;
    min-height: 900px;
  }
  
  .facilities-row {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .rooms-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .footer-content {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .links {
    display: none;
  }
  
  .nav-links {
    justify-content: space-between;
  }
  
  .hero-title {
    font-size: 48px;
  }
  
  .brand-name {
    font-size: 44px;
  }
  
  .hero-subtitle {
    font-size: 16px;
  }
  
  .hero-image {
    height: 450px;
  }
  
  .facilities-row {
    grid-template-columns: 1fr;
  }
  
  .rooms-grid {
    grid-template-columns: 1fr;
  }
  
  .testimonials-grid {
    grid-template-columns: 1fr;
  }
  
  .booking-controls {
    flex-wrap: wrap;
    height: auto;
    padding: 15px 0;
  }
  
  .quick-booking {
    height: auto;
    min-height: 150px;
  }
  
  .footer-content {
    grid-template-columns: 1fr;
  }
  
  .footer {
    height: auto;
    min-height: 800px;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 36px;
  }
  
  .brand-name {
    font-size: 36px;
  }
  
  .hero-subtitle {
    font-size: 15px;
  }
  
  .hero-image {
    height: 350px;
  }
  
  .facilities-title,
  .rooms-title,
  .testimonials-title {
    font-size: 32px;
  }
  
  .book-btn,
  .book-btn-hero {
    width: 160px;
    height: 52px;
  }
  
  .btn-text,
  .btn-text-hero {
    font-size: 15px;
    left: 50%;
    transform: translateX(-50%);
  }
}
</style>
