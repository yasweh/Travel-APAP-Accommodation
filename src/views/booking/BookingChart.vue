<template>
  <div class="booking-chart">
    <h1>Booking Statistics</h1>

    <!-- Error Message -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <!-- Month/Year Selection -->
    <div class="filters">
      <div class="form-group">
        <label for="month">Month</label>
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
        <label for="year">Year</label>
        <input type="number" id="year" v-model.number="selectedYear" min="2020" max="2030" />
      </div>

      <button @click="loadStatistics" class="btn-primary">Load Statistics</button>
      <button @click="goBack" class="btn-secondary">Back to List</button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">Loading statistics...</div>

    <!-- Chart -->
    <div v-else-if="statistics.length > 0" class="chart-container">
      <canvas ref="chartCanvas"></canvas>
    </div>

    <!-- Empty State -->
    <div v-else-if="!loading" class="empty-state">
      <p>No statistics data for {{ getMonthName(selectedMonth) }} {{ selectedYear }}.</p>
      <p class="hint">
        Make sure there are completed bookings (status = DONE) with check-in dates in {{ getMonthName(selectedMonth) }} {{ selectedYear }}.
      </p>
      <p class="debug-info">
        Check browser console (F12) for detailed debugging information.
      </p>
    </div>

    <!-- Statistics Table -->
    <div v-if="statistics.length > 0" class="stats-table">
      <h2>Detailed Statistics</h2>
      <table class="data-table">
        <thead>
          <tr>
            <th>Rank</th>
            <th>Property</th>
            <th>Total Income</th>
            <th>Booking Count</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(stat, index) in statistics" :key="stat.propertyId">
            <td>{{ index + 1 }}</td>
            <td>{{ stat.propertyName }}</td>
            <td>Rp {{ formatCurrency(stat.totalIncome) }}</td>
            <td>{{ stat.bookingCount }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
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
      console.log('Statistics data:', statistics.value)
      
      if (statistics.value.length === 0) {
        error.value = `No data found for ${getMonthName(selectedMonth.value)} ${selectedYear.value}. Make sure there are DONE bookings (status=4) with check-in dates in this month.`
      } else {
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
  if (!chartCanvas.value) return

  // Destroy previous chart instance
  if (chartInstance) {
    chartInstance.destroy()
  }

  if (statistics.value.length === 0) return

  const ctx = chartCanvas.value.getContext('2d')
  if (!ctx) return

  const labels = statistics.value.map((s) => s.propertyName)
  const data = statistics.value.map((s) => s.totalIncome)

  chartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: labels,
      datasets: [
        {
          label: 'Total Income (Rp)',
          data: data,
          backgroundColor: 'rgba(76, 175, 80, 0.6)',
          borderColor: 'rgba(76, 175, 80, 1)',
          borderWidth: 2,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: true,
          position: 'top',
        },
        title: {
          display: true,
          text: `Property Income Statistics - ${getMonthName(selectedMonth.value)} ${selectedYear.value}`,
          font: {
            size: 16,
          },
        },
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            callback: function (value) {
              return 'Rp ' + value.toLocaleString('id-ID')
            },
          },
        },
      },
    },
  })
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
.booking-chart {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  color: #333;
  margin-bottom: 20px;
}

h2 {
  color: #555;
  margin-top: 40px;
  margin-bottom: 15px;
}

.error-message {
  background-color: #fee;
  color: #c33;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.filters {
  display: flex;
  gap: 15px;
  align-items: flex-end;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: bold;
  margin-bottom: 5px;
  color: #333;
}

.form-group select,
.form-group input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 120px;
}

.btn-primary,
.btn-secondary {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  align-self: flex-end;
}

.btn-primary {
  background-color: #4caf50;
  color: white;
}

.btn-primary:hover {
  background-color: #45a049;
}

.btn-secondary {
  background-color: #999;
  color: white;
}

.btn-secondary:hover {
  background-color: #777;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.chart-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  height: 400px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-state p {
  font-size: 16px;
  margin: 10px 0;
}

.empty-state .hint {
  color: #666;
  font-size: 14px;
  margin-top: 15px;
}

.empty-state .debug-info {
  color: #999;
  font-size: 12px;
  font-style: italic;
  margin-top: 20px;
}

.stats-table {
  margin-top: 40px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.data-table th {
  background-color: #f5f5f5;
  font-weight: bold;
  color: #333;
}

.data-table tbody tr:hover {
  background-color: #f9f9f9;
}

.data-table tbody tr:nth-child(1) {
  background-color: #fff9c4; /* Gold for rank 1 */
}

.data-table tbody tr:nth-child(2) {
  background-color: #e0e0e0; /* Silver for rank 2 */
}

.data-table tbody tr:nth-child(3) {
  background-color: #ffccbc; /* Bronze for rank 3 */
}
</style>
