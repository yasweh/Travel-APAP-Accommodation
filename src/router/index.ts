import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
    },

    // Property Management
    {
      path: '/property',
      name: 'property-list',
      component: () => import('../views/property/PropertyList.vue'),
    },
    {
      path: '/property/create',
      name: 'property-create',
      component: () => import('../views/property/PropertyForm.vue'),
    },
    {
      path: '/property/:id',
      name: 'property-detail',
      component: () => import('../views/property/PropertyDetail.vue'),
    },
    {
      path: '/property/edit/:id',
      name: 'property-edit',
      component: () => import('../views/property/PropertyForm.vue'),
    },

    // Room Type Management
    {
      path: '/room-type',
      name: 'room-type-list',
      component: () => import('../views/room-type/RoomTypeList.vue'),
    },
    

    // Booking Management
    {
      path: '/booking',
      name: 'booking-list',
      component: () => import('../views/booking/BookingList.vue'),
    },
    {
      path: '/booking/create',
      name: 'booking-create',
      component: () => import('../views/booking/BookingCreate.vue'),
    },
    {
      path: '/booking/:id',
      name: 'booking-detail',
      component: () => import('../views/booking/BookingDetail.vue'),
    },
    {
      path: '/booking/update/:id',
      name: 'booking-update',
      component: () => import('../views/booking/BookingUpdate.vue'),
    },
    {
      path: '/booking/chart',
      name: 'booking-chart',
      component: () => import('../views/booking/BookingChart.vue'),
    },

    // Maintenance Management
    {
      path: '/maintenance',
      name: 'maintenance-list',
      component: () => import('../views/maintenance/MaintenanceList.vue'),
    },
    {
      path: '/maintenance/create',
      name: 'maintenance-create',
      component: () => import('../views/maintenance/MaintenanceForm.vue'),
    },
  ],
})

export default router
