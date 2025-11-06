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

const menuItems = [
  {
    title: 'Property Management',
    description: 'Manage hotels and accommodations',
    icon: '🏨',
    path: '/property',
    color: '#4caf50'
  },
  {
    title: 'Room Type Management',
    description: 'Configure room types and facilities',
    icon: '🛏️',
    path: '/room-type',
    color: '#2196f3'
  },
  {
    title: 'Booking Management',
    description: 'Handle reservations and payments',
    icon: '📅',
    path: '/booking',
    color: '#ff9800'
  },
  {
    title: 'Maintenance Schedule',
    description: 'Schedule room maintenance',
    icon: '🔧',
    path: '/maintenance',
    color: '#9c27b0'
  }
]

onMounted(() => {
  loadStatistics()
})
</script>

<template>
  <div class="home-container">
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">🏨 Accommodation Management System</h1>
        <p class="hero-subtitle">Comprehensive solution for managing properties, bookings, and maintenance</p>
        <div class="hero-stats">
          <div class="stat-card">
            <div class="stat-number">{{ loading ? '...' : stats.properties }}</div>
            <div class="stat-label">Properties</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ loading ? '...' : stats.bookings }}</div>
            <div class="stat-label">Bookings</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ loading ? '...' : stats.roomTypes }}</div>
            <div class="stat-label">Room Types</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">24/7</div>
            <div class="stat-label">Support</div>
          </div>
        </div>
      </div>
    </div>

    <div class="content-wrapper">
      <div class="menu-section">
        <h2 class="section-title">Quick Access</h2>
        <div class="menu-grid">
          <div 
            v-for="item in menuItems" 
            :key="item.path"
            class="menu-card"
            :style="{ borderTopColor: item.color }"
            @click="navigateTo(item.path)"
          >
            <div class="menu-icon">{{ item.icon }}</div>
            <h3 class="menu-title">{{ item.title }}</h3>
            <p class="menu-description">{{ item.description }}</p>
            <button class="menu-button" :style="{ backgroundColor: item.color }">
              Go to {{ item.title }}
            </button>
          </div>
        </div>
      </div>

      <div class="features-section">
        <h2 class="section-title">Key Features</h2>
        <div class="features-grid">
          <div class="feature-item">
            <div class="feature-icon">📊</div>
            <h4>Real-time Statistics</h4>
            <p>View booking analytics and revenue charts</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">💳</div>
            <h4>Payment Management</h4>
            <p>Handle payments, refunds, and cancellations</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🔍</div>
            <h4>Smart Search</h4>
            <p>Find rooms, bookings, and schedules easily</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">⚡</div>
            <h4>Auto Check-in</h4>
            <p>Automated daily check-in scheduler</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🎯</div>
            <h4>Cascading Filters</h4>
            <p>Smart property and room selection</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🔔</div>
            <h4>Maintenance Alerts</h4>
            <p>Schedule and track room maintenance</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  width: 100%;
  min-height: 100vh;
}

/* Hero Section - Full Width */
.hero-section {
  width: 100%;
  background: linear-gradient(135deg, #4caf50 0%, #2196f3 100%);
  color: white;
  padding: 80px 0;
  margin-bottom: 60px;
}

.hero-content {
  -width: 1400px;
  margin: 0 auto;
  padding: 0 40px;
  text-align: center;
}

.hero-title {
  font-size: 52px;
  font-weight: bold;
  margin: 0 0 24px 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.hero-subtitle {
  font-size: 22px;
  margin: 0 0 50px 0;
  opacity: 0.95;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 24px;
  max-width: 1000px;
  margin: 0 auto;
}

.stat-card {
  background: rgba(255, 255, 255, 0.2);
  padding: 35px 25px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-8px);
  background: rgba(255, 255, 255, 0.3);
}

.stat-number {
  font-size: 42px;
  font-weight: bold;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 18px;
  opacity: 0.9;
}

/* Content Wrapper */
.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px 60px;
}

/* Menu Section */
.menu-section {
  margin-bottom: 80px;
}

.section-title {
  text-align: center;
  font-size: 42px;
  margin: 0 0 50px 0;
  color: #333;
  font-weight: 600;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 35px;
}

.menu-card {
  background: white;
  border-radius: 16px;
  padding: 45px 35px;
  text-align: center;
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.1);
  transition: all 0.4s ease;
  cursor: pointer;
  border-top: 5px solid #4caf50;
  position: relative;
  overflow: hidden;
}

.menu-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--card-color, #4caf50), transparent);
}

.menu-card:hover {
  transform: translateY(-12px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
}

.menu-icon {
  font-size: 72px;
  margin-bottom: 25px;
  transition: transform 0.3s ease;
}

.menu-card:hover .menu-icon {
  transform: scale(1.1);
}

.menu-title {
  font-size: 26px;
  margin: 0 0 18px 0;
  color: #333;
  font-weight: 600;
}

.menu-description {
  font-size: 17px;
  color: #666;
  margin: 0 0 30px 0;
  line-height: 1.6;
}

.menu-button {
  padding: 14px 35px;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 17px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
}

.menu-button:hover {
  opacity: 0.9;
  transform: scale(1.02);
}

/* Features Section */
.features-section {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 20px;
  padding: 60px 50px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 35px;
}

.feature-item {
  text-align: center;
  padding: 30px 25px;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: white;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.feature-item:hover {
  background: white;
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 56px;
  margin-bottom: 20px;
  transition: transform 0.3s ease;
}

.feature-item:hover .feature-icon {
  transform: scale(1.1);
}

.feature-item h4 {
  font-size: 22px;
  margin: 0 0 15px 0;
  color: #333;
  font-weight: 600;
}

.feature-item p {
  font-size: 16px;
  color: #666;
  margin: 0;
  line-height: 1.6;
}

/* Responsive Design untuk Desktop First */
@media (max-width: 1200px) {
  .hero-content,
  .content-wrapper {
    padding: 0 30px;
  }
  
  .hero-title {
    font-size: 46px;
  }
  
  .menu-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 60px 0;
  }
  
  .hero-content,
  .content-wrapper {
    padding: 0 20px;
  }
  
  .hero-title {
    font-size: 36px;
  }
  
  .hero-subtitle {
    font-size: 18px;
  }
  
  .section-title {
    font-size: 32px;
  }
  
  .menu-grid {
    grid-template-columns: 1fr;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .hero-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 28px;
  }
  
  .hero-stats {
    grid-template-columns: 1fr;
  }
  
  .menu-card,
  .feature-item {
    padding: 30px 20px;
  }
}
</style>