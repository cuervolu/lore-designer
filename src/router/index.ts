import { createRouter, createWebHistory } from 'vue-router';

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // Redirect root to wizard
    {
      path: '/',
      redirect: '/wizard'
    },

    // Wizard module
    {
      path: '/wizard',
      name: 'wizard',
      component: () => import('@wizard/layouts/WizardLayout.vue'),
      children: [
        {
          path: '',
          redirect: { name: 'workspaces' }
        },
        {
          path: 'workspaces',
          name: 'workspaces',
          component: () => import('@wizard/views/WorkspacesView.vue'),
        },
        {
          path: 'new-workspace',
          name: 'new-workspace',
          component: () => import('@wizard/views/NewWorkspaceView.vue'),
        },
        {
          path: 'plugins',
          name: 'plugins',
          component: () => import('@wizard/views/PluginsView.vue'),
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('@wizard/views/SettingsView.vue'),
        },
        {
          path: 'learn',
          name: 'learn',
          component: () => import('@wizard/views/LearnView.vue'),
        }
      ]
    },

    // Not Found - Add this when you have a 404 page
    {
      path: '/:pathMatch(.*)*',
      redirect: '/wizard'
      // Uncomment when you create a 404 page
      // component: () => import('@/modules/common/pages/NotFound404.vue'),
    }
  ]
});

export default router;
