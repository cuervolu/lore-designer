import { createRouter, createWebHistory } from 'vue-router';
import WizardLayout from '@/features/workspaces/WizardLayout.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // Redirect root to wizard
    {
      path: '/',
      redirect: '/wizard',
    },
    // Wizard routes
    {
      path: '/wizard',
      name: 'wizard',
      component: WizardLayout,
      children: [
        {
          path: '',
          name: 'workspaces',
          component: () => import('@/features/workspaces/WorkspacesView.vue'),
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/wizard',
      // TODO: Create a 404 page and an general error page
      // component: () => import('@/modules/common/pages/NotFound404.vue'),
    },
  ],
});

export default router;
