<template>
  <div class="booking-chart">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <path d="M36 0H4C1.8 0 0 1.8 0 4V36C0 38.2 1.8 40 4 40H36C38.2 40 40 38.2 40 36V4C40 1.8 38.2 0 36 0ZM16 32H12V16H16V32ZM22 32H18V8H22V32ZM28 32H24V22H28V32Z" fill="#7C6A46"/>
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">Booking Statistics</h1>
          <p class="page-subtitle">Analyze property performance and booking trends</p>
        </div>
      </div>
    </div>

    <!-- Error Message -->
    <div v-if="error" class="error-message">
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM11 15H9V13H11V15ZM11 11H9V5H11V11Z" fill="#F44336"/>
      </svg>
      <span>{{ error }}</span>
    </div>

    <!-- Filters -->
    <div class="filters-container">
      <div class="filter-card">
        <div class="filter-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M20 3H4C2.9 3 2 3.9 2 5V19C2 20.1 2.9 21 4 21H20C21.1 21 22 20.1 22 19V5C22 3.9 21.1 3 20 3ZM20 19H4V8H20V19Z" fill="#7C6A46"/>
          </svg>
          <span class="filter-title">Select Period</span>
        </div>
        <div class="filter-inputs">
          <div class="form-group">
            <label for="month">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M14 2H12V0H10V2H6V0H4V2H2C0.9 2 0 2.9 0 4V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V4C16 2.9 15.1 2 14 2Z" fill="#7C6A46"/>
              </svg>
              Month
            </label>
            <select id="month" v-model.number="selectedMonth">
              <option :value="1">January</option>
              <option :value="2">February</option>
              <option :value="3">March</option>
              <option :value="4">April</option>
              <option :value="5">May</option>
              <option :value="6">June</option>
              <option :value="7">July</option>
              <option :value="8">August</option>
              <option :value="9">September</option>
              <option :value="10">October</option>
              <option :value="11">November</option>
              <option :value="12">December</option>
            </select>
          </div>

          <div class="form-group">
            <label for="year">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 0C3.6 0 0 3.6 0 8C0 12.4 3.6 16 8 16C12.4 16 16 12.4 16 8C16 3.6 12.4 0 8 0ZM8 2C8.6 2 9 2.4 9 3V8L12 10L11 11.5L7.5 9V3C7.5 2.4 7.9 2 8 2Z" fill="#7C6A46"/>
              </svg>
              Year
            </label>
            <input type="number" id="year" v-model.number="selectedYear" min="2020" max="2030" />
          </div>

          <button @click="loadStatistics" class="btn-load">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M10 0L0 5V7L10 12L20 7V5L10 0ZM0 9V11L10 16L20 11V9L10 14L0 9Z" fill="white"/>
            </svg>
            Load Statistics
          </button>
        </div>
      </div>

      <button @click="goBack" class="btn-back">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M20 9H6L11 4L10 2L2 10L10 18L11 16L6 11H20V9Z" fill="#7C6A46"/>
        </svg>
        Back to List
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <p>Loading statistics...</p>
    </div>

    <!-- Chart and Stats -->
    <div v-else-if="statistics.length > 0" class="stats-content">
      <!-- Summary Cards -->
      <div class="summary-cards">
        <div class="summary-card">
          <div class="card-icon income">
            <svg width="30" height="30" viewBox="0 0 30 30" fill="none">
              <path d="M15 0C6.7 0 0 6.7 0 15C0 23.3 6.7 30 15 30C23.3 30 30 23.3 30 15C30 6.7 23.3 0 15 0ZM16.5 21H13.5V18H16.5V21ZM16.5 15H13.5V9H16.5V15Z" fill="white"/>
            </svg>
          </div>
          <div class="card-content">
            <div class="card-label">Total Income</div>
            <div class="card-value">Rp {{ formatCurrency(totalIncome) }}</div>
          </div>
        </div>

        <div class="summary-card">
          <div class="card-icon bookings">
            <svg width="30" height="30" viewBox="0 0 30 30" fill="none">
              <path d="M0 25.5C0 28.05 2.05 30 4.5 30H25.5C28.05 30 30 28.05 30 25.5V13.5H0V25.5ZM25.5 3H22.5V1.5C22.5 0.6 21.9 0 21 0C20.1 0 19.5 0.6 19.5 1.5V3H10.5V1.5C10.5 0.6 9.9 0 9 0C8.1 0 7.5 0.6 7.5 1.5V3H4.5C2.05 3 0 4.95 0 7.5V10.5H30V7.5C30 4.95 28.05 3 25.5 3Z" fill="white"/>
            </svg>
          </div>
          <div class="card-content">
            <div class="card-label">Total Bookings</div>
            <div class="card-value">{{ totalBookings }}</div>
          </div>
        </div>

        <div class="summary-card">
          <div class="card-icon properties">
            <svg width="30" height="30" viewBox="0 0 30 30" fill="none">
              <path d="M15 0L0 11.25H3.75L15 3.75L26.25 11.25H30L15 0ZM7.5 7.5V0H11.25V12.5L7.5 7.5Z" fill="white"/>
            </svg>
          </div>
          <div class="card-content">
            <div class="card-label">Active Properties</div>
            <div class="card-value">{{ statistics.length }}</div>
          </div>
        </div>
      </div>

      <!-- Chart -->
      <div class="chart-card">
        <div class="chart-header">
          <h2>Property Income Comparison</h2>
          <span class="chart-period">{{ getMonthName(selectedMonth) }} {{ selectedYear }}</span>
        </div>
        <div class="chart-container">
          <canvas ref="chartCanvas"></canvas>
        </div>
      </div>

      <!-- Leaderboard -->
      <div class="leaderboard-card">
        <div class="leaderboard-header">
          <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
            <path d="M14 0L17.5 10.5H28L19.5 17L23 27.5L14 21L5 27.5L8.5 17L0 10.5H10.5L14 0Z" fill="#FFD700"/>
          </svg>
          <h2>Property Leaderboard</h2>
        </div>
        <div class="leaderboard-items">
          <div 
            v-for="(stat, index) in statistics" 
            :key="stat.propertyId" 
            :class="['leaderboard-item', `rank-${index + 1}`]"
          >
            <div class="rank-badge">
              <svg v-if="index === 0" width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M12 0L15 9H24L16.5 14.5L19.5 23.5L12 18L4.5 23.5L7.5 14.5L0 9H9L12 0Z" fill="#FFD700"/>
              </svg>
              <svg v-else-if="index === 1" width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M12 0L15 9H24L16.5 14.5L19.5 23.5L12 18L4.5 23.5L7.5 14.5L0 9H9L12 0Z" fill="#C0C0C0"/>
              </svg>
              <svg v-else-if="index === 2" width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M12 0L15 9H24L16.5 14.5L19.5 23.5L12 18L4.5 23.5L7.5 14.5L0 9H9L12 0Z" fill="#CD7F32"/>
              </svg>
              <span v-else class="rank-number">{{ index + 1 }}</span>
            </div>
            <div class="property-info">
              <div class="property-name">{{ stat.propertyName }}</div>
              <div class="property-stats">
                <span class="stat-item">
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M7 0C3.1 0 0 3.1 0 7C0 10.9 3.1 14 7 14C10.9 14 14 10.9 14 7C14 3.1 10.9 0 7 0Z" fill="#4CAF50"/>
                  </svg>
                  {{ stat.bookingCount }} bookings
                </span>
              </div>
            </div>
            <div class="property-income">Rp {{ formatCurrency(stat.totalIncome) }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="!loading" class="empty-state">
      <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
        <path d="M72 0H8C3.6 0 0 3.6 0 8V72C0 76.4 3.6 80 8 80H72C76.4 80 80 76.4 80 72V8C80 3.6 76.4 0 72 0ZM32 64H24V32H32V64ZM44 64H36V16H44V64ZM56 64H48V44H56V64Z" fill="#D9D9D9"/>
      </svg>
      <h3>No Statistics Data</h3>
      <p>No statistics available for {{ getMonthName(selectedMonth) }} {{ selectedYear }}</p>
      <p class="hint">Make sure there are confirmed bookings (Payment Confirmed) with check-in dates in this period</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Chart, registerables } from 'chart.js'
import { bookingService, type PropertyStatisticsDTO } from '@/services/bookingService'

Chart.register(...registerables)

const router = useRouter()

const statistics = ref<PropertyStatisticsDTO[]>([])
const loading = ref(false)
const error = ref('')

const selectedMonth = ref(new Date().getMonth() + 1)
const selectedYear = ref(new Date().getFullYear())

const chartCanvas = ref<HTMLCanvasElement | null>(null)
let chartInstance: Chart | null = null

// Computed properties
const totalIncome = computed(() => {
  return statistics.value.reduce((sum, stat) => sum + stat.totalIncome, 0)
})

const totalBookings = computed(() => {
  return statistics.value.reduce((sum, stat) => sum + stat.bookingCount, 0)
})

const loadStatistics = async () => {
  loading.value = true
  error.value = ''
  try {
    console.log('Loading statistics for:', selectedMonth.value, selectedYear.value)
    const response = await bookingService.getMonthlyStatistics(
      selectedMonth.value,
      selectedYear.value
    )
    console.log('Response received:', response)
    
    if (response.success) {
      statistics.value = response.data
      
      if (statistics.value.length != 0) {
        // Use nextTick to ensure DOM is updated before rendering chart
        await nextTick()
        renderChart()
      }
    } else {
      error.value = response.message || 'Failed to load statistics'
      statistics.value = []
    }
  } catch (err: any) {
    console.error('Load statistics error:', err)
    error.value = err.response?.data?.message || err.message || 'Failed to load statistics'
    statistics.value = []
  } finally {
    loading.value = false
  }
}

const renderChart = () => {
  console.log('renderChart called')
  console.log('chartCanvas.value:', chartCanvas.value)
  
  if (!chartCanvas.value) {
    console.log('Canvas not found - waiting...')
    // Try again after a short delay
    setTimeout(() => {
      if (chartCanvas.value) {
        console.log('Canvas now available, retrying...')
        renderChart()
      }
    }, 100)
    return
  }

  // Destroy previous chart instance
  if (chartInstance) {
    console.log('Destroying previous chart')
    chartInstance.destroy()
    chartInstance = null
  }

  if (statistics.value.length === 0) {
    console.log('No statistics data')
    return
  }

  const ctx = chartCanvas.value.getContext('2d')
  if (!ctx) {
    console.log('Could not get canvas context')
    return
  }

  const labels = statistics.value.map((s) => s.propertyName)
  const data = statistics.value.map((s) => s.totalIncome)

  console.log('Creating chart with data:', { labels, data })

  try {
    chartInstance = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [
          {
            label: 'Total Income (Rp)',
            data: data,
            backgroundColor: 'rgba(124, 106, 70, 0.8)',
            borderColor: 'rgba(124, 106, 70, 1)',
            borderWidth: 2,
            borderRadius: 8,
            hoverBackgroundColor: 'rgba(124, 106, 70, 0.9)',
            hoverBorderColor: 'rgba(90, 74, 48, 1)',
            hoverBorderWidth: 3,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        aspectRatio: 2,
        plugins: {
          legend: {
            display: true,
            position: 'top',
            labels: {
              font: {
                family: 'Poppins',
                size: 14,
                weight: 'bold'
              },
              color: '#1C1C1C',
              padding: 15,
              usePointStyle: true,
              pointStyle: 'rectRounded'
            }
          },
          tooltip: {
            enabled: true,
            backgroundColor: 'rgba(28, 28, 28, 0.9)',
            titleColor: '#FFFFFF',
            bodyColor: '#FFFFFF',
            titleFont: {
              family: 'Raleway',
              size: 14,
              weight: 'bold'
            },
            bodyFont: {
              family: 'Poppins',
              size: 13,
              weight: 'normal'
            },
            padding: 12,
            cornerRadius: 8,
            callbacks: {
              label: function(context) {
                const value = context.parsed.y || 0
                return 'Income: Rp ' + value.toLocaleString('id-ID')
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: function (value) {
                return 'Rp ' + value.toLocaleString('id-ID')
              },
              font: {
                family: 'Poppins',
                size: 12
              },
              color: '#666'
            },
            grid: {
              color: 'rgba(0, 0, 0, 0.08)'
            }
          },
          x: {
            ticks: {
              font: {
                family: 'Poppins',
                size: 12,
                weight: 'normal'
              },
              color: '#1C1C1C',
              maxRotation: 45,
              minRotation: 0
            },
            grid: {
              display: false
            }
          }
        },
      },
    })

    console.log('Chart created successfully:', chartInstance)
  } catch (error) {
    console.error('Error creating chart:', error)
  }
}

const goBack = () => {
  router.push('/booking')
}

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('id-ID').format(value)
}

const getMonthName = (month: number) => {
  const months = [
    'January',
    'February',
    'March',
    'April',
    'May',
    'June',
    'July',
    'August',
    'September',
    'October',
    'November',
    'December',
  ]
  return months[month - 1]
}

onMounted(() => {
  loadStatistics()
})

watch([selectedMonth, selectedYear], () => {
  // Auto-reload when month/year changes (optional)
  // loadStatistics()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&family=Raleway:wght@500;600;700;800&display=swap');

.booking-chart {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 60px;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Page Header */
.page-header {
  margin-bottom: 40px;
  padding: 30px 40px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F0EDE6 100%);
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(124, 106, 70, 0.1);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 25px;
}

.header-icon {
  width: 80px;
  height: 80px;
  background: white;
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(124, 106, 70, 0.15);
}

.header-text {
  flex: 1;
}

.page-title {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 42px;
  font-weight: 800;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.page-subtitle {
  color: #4A4A4A;
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  opacity: 0.9;
}

/* Error Message */
.error-message {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #FEE;
  color: #C33;
  padding: 16px 20px;
  border-radius: 10px;
  margin-bottom: 25px;
  border-left: 4px solid #F44336;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 500;
}

/* Filters */
.filters-container {
  display: flex;
  gap: 20px;
  margin-bottom: 35px;
  align-items: flex-start;
}

.filter-card {
  flex: 1;
  background: white;
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #F0F0F0;
}

.filter-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.filter-title {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 20px;
  font-weight: 700;
}

.filter-inputs {
  display: flex;
  gap: 15px;
  align-items: flex-end;
}

.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 600;
}

.form-group select,
.form-group input {
  padding: 12px 16px;
  border: 2px solid #F0F0F0;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 500;
  color: #1C1C1C;
  transition: all 0.3s ease;
  background: #FAFAFA;
}

.form-group select:focus,
.form-group input:focus {
  outline: none;
  border-color: #7C6A46;
  background: white;
}

.btn-load {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #7C6A46 0%, #9B8A68 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(124, 106, 70, 0.3);
}

.btn-load:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(124, 106, 70, 0.4);
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  background: white;
  color: #7C6A46;
  border: 2px solid #7C6A46;
  border-radius: 8px;
  cursor: pointer;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-back:hover {
  background: #FAFAFA;
  transform: translateY(-2px);
}

/* Loading */
.loading {
  text-align: center;
  padding: 80px 20px;
  color: #7C6A46;
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 500;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  margin: 0 auto 20px;
  border: 4px solid #FAFAFA;
  border-top: 4px solid #7C6A46;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Stats Content */
.stats-content {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

/* Summary Cards */
.summary-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.summary-card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #F0F0F0;
  transition: all 0.3s ease;
}

.summary-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(124, 106, 70, 0.15);
}

.card-icon {
  width: 70px;
  height: 70px;
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-icon.income {
  background: linear-gradient(135deg, #4CAF50 0%, #388E3C 100%);
}

.card-icon.bookings {
  background: linear-gradient(135deg, #2196F3 0%, #1976D2 100%);
}

.card-icon.properties {
  background: linear-gradient(135deg, #7C6A46 0%, #9B8A68 100%);
}

.card-content {
  flex: 1;
}

.card-label {
  color: #666;
  font-family: 'Poppins', sans-serif;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 5px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.card-value {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 28px;
  font-weight: 800;
}

/* Chart Card */
.chart-card {
  background: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #F0F0F0;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.chart-header h2 {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.chart-period {
  color: #7C6A46;
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 600;
  padding: 8px 16px;
  background: #FAFAFA;
  border-radius: 8px;
}

.chart-container {
  height: 450px;
  width: 100%;
  position: relative;
  padding: 20px;
  background: #FAFAFA;
  border-radius: 12px;
}

.chart-container canvas {
  display: block !important;
  width: 100% !important;
  height: 100% !important;
  max-height: 450px;
}

/* Leaderboard */
.leaderboard-card {
  background: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #F0F0F0;
}

.leaderboard-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 25px;
}

.leaderboard-header h2 {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.leaderboard-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.leaderboard-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: #FAFAFA;
  border: 2px solid transparent;
}

.leaderboard-item:hover {
  transform: translateX(10px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.leaderboard-item.rank-1 {
  background: linear-gradient(135deg, #FFF9E6 0%, #FFECB3 100%);
  border-color: #FFD700;
}

.leaderboard-item.rank-2 {
  background: linear-gradient(135deg, #F5F5F5 0%, #E0E0E0 100%);
  border-color: #C0C0C0;
}

.leaderboard-item.rank-3 {
  background: linear-gradient(135deg, #FFE8DC 0%, #FFCCBC 100%);
  border-color: #CD7F32;
}

.rank-badge {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.rank-number {
  color: #7C6A46;
  font-family: 'Raleway', sans-serif;
  font-size: 24px;
  font-weight: 800;
}

.property-info {
  flex: 1;
}

.property-name {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 5px;
}

.property-stats {
  display: flex;
  gap: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-family: 'Poppins', sans-serif;
  font-size: 13px;
  font-weight: 500;
}

.property-income {
  color: #7C6A46;
  font-family: 'Raleway', sans-serif;
  font-size: 20px;
  font-weight: 800;
  white-space: nowrap;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 80px 40px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F0EDE6 100%);
  border-radius: 15px;
  margin-top: 20px;
}

.empty-state svg {
  margin-bottom: 25px;
  opacity: 0.4;
}

.empty-state h3 {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px 0;
}

.empty-state p {
  color: #666;
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 400;
  margin: 8px 0;
}

.empty-state .hint {
  color: #999;
  font-size: 14px;
  font-style: italic;
  margin-top: 20px;
}

/* Responsive */
@media (max-width: 1200px) {
  .summary-cards {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .booking-chart {
    padding: 20px;
  }

  .page-header {
    padding: 20px;
  }

  .header-content {
    flex-direction: column;
    text-align: center;
  }

  .page-title {
    font-size: 32px;
  }

  .filters-container {
    flex-direction: column;
  }

  .filter-inputs {
    flex-direction: column;
  }

  .form-group {
    width: 100%;
  }

  .btn-load,
  .btn-back {
    width: 100%;
    justify-content: center;
  }

  .chart-container {
    height: 300px;
  }

  .leaderboard-item {
    flex-wrap: wrap;
  }

  .property-income {
    width: 100%;
    text-align: center;
    margin-top: 10px;
  }
}
</style>
