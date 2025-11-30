import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/auth/LoginView.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },

    {
      path: '/property',
      name: 'property-list',
      component: () => import('../views/property/PropertyList.vue'),
    },
    {
      path: '/property/create',
      name: 'property-create',
      component: () => import('../views/property/PropertyForm.vue'),
      meta: { requiresAuth: true, roles: ['Superadmin', 'Accommodation Owner'] }
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
      meta: { requiresAuth: true, roles: ['Superadmin', 'Accommodation Owner'] }
    },

    // Room Type Management
    {
      path: '/room-type',
      name: 'room-type-list',
      component: () => import('../views/room-type/RoomTypeList.vue'),
      meta: { requiresAuth: true }
    },
    

    // Booking Management
    {
      path: '/booking',
      name: 'booking-list',
      component: () => import('../views/booking/BookingList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/booking/create',
      name: 'booking-create',
      component: () => import('../views/booking/BookingCreate.vue'),
      meta: { requiresAuth: true } // Allow all authenticated users to create booking
    },
    {
      path: '/booking/:id',
      name: 'booking-detail',
      component: () => import('../views/booking/BookingDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/booking/update/:id',
      name: 'booking-update',
      component: () => import('../views/booking/BookingUpdate.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/booking/chart',
      name: 'booking-chart',
      component: () => import('../views/booking/BookingChart.vue'),
      meta: { requiresAuth: true, roles: ['Superadmin', 'Accommodation Owner'] }
    },

    // Maintenance Management
    {
      path: '/maintenance',
      name: 'maintenance-list',
      component: () => import('../views/maintenance/MaintenanceList.vue'),
      meta: { requiresAuth: true, roles: ['Superadmin', 'Accommodation Owner'] }
    },
    {
      path: '/maintenance/create',
      name: 'maintenance-create',
      component: () => import('../views/maintenance/MaintenanceForm.vue'),
      meta: { requiresAuth: true, roles: ['Superadmin', 'Accommodation Owner'] }
    },

    // Support Ticket Management
    {
      path: '/support',
      name: 'support-dashboard',
      component: () => import('../views/support/SupportDashboardView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/support/tickets/:id',
      name: 'support-ticket-detail',
      component: () => import('../views/support/TicketDetailView.vue'),
      meta: { requiresAuth: true }
    },

    // Review Management
    {
      path: '/reviews',
      name: 'all-reviews',
      component: () => import('../views/review/ReviewListView.vue'),
    },
    {
      path: '/reviews/create',
      name: 'review-create',
      component: () => import('../views/review/ReviewFormView.vue'),
      meta: { requiresAuth: true, roles: ['Customer'] }
    },
    {
      path: '/reviews/:reviewId',
      name: 'review-detail',
      component: () => import('../views/review/ReviewDetailView.vue'),
    },
    {
      path: '/reviews/property/:propertyId',
      name: 'property-reviews',
      component: () => import('../views/review/ReviewListView.vue'),
    },
    {
      path: '/reviews/my-reviews',
      name: 'my-reviews',
      component: () => import('../views/review/ReviewListView.vue'),
      meta: { requiresAuth: true }
    },
  ],
})

// Navigation guards
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // Check if route requires authentication
  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      // Redirect to login with return URL
      next({
        name: 'login',
        query: { redirect: to.fullPath }
      })
      return
    }
    
    // Check role-based access
    if (to.meta.roles && Array.isArray(to.meta.roles)) {
      const userRole = authStore.userRole
      if (!userRole || !to.meta.roles.includes(userRole)) {
        // User doesn't have required role
        alert('Access denied: You do not have permission to access this page')
        next('/')
        return
      }
    }
  }
  
  // Redirect authenticated users away from login page
  if (to.meta.requiresGuest && authStore.isAuthenticated) {
    next('/')
    return
  }
  
  next()
})

export default router
