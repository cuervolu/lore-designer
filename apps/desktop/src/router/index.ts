import { createRouter, createWebHistory } from 'vue-router';
import WizardLayout from '@/features/workspaces/WizardLayout.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: WizardLayout,
      children: [
        {
          path: '',
          name: 'workspaces',
          component: () => import('@/features/workspaces/WorkspacesView.vue'),
        }
      ],
    },
  ],
});

export default router;
