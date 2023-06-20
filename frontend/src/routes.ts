import {createRouter,createWebHashHistory} from "vue-router";

import HomePage from "@/pages/HomePage.vue";
import ReviewApplicationPage from "@/pages/ReviewApplicationPage.vue";
import ApplyClientNumber from "@/pages/ApplyClientNumberPage.vue";
import FormSubmittedPage from "@/pages/FormSubmittedPage.vue";


const routes = [
  { 
    path: '/', 
    name:'home', 
    component: HomePage,
    props: true
  },
  { 
    path: '/new-client',
    name:'form',
    component: ApplyClientNumber,
    props: true
   },
  { 
    path: '/submissions',
    name:'internal',
    component: ReviewApplicationPage,
    props: true
   },
  { 
    path: '/form-submitted',
    name:'confirmation',
    component: FormSubmittedPage,
    props: true
   },
];


export const router = createRouter({
  // 4. Provide the history implementation to use. We are using the hash history for simplicity here.
  history: createWebHashHistory(),
  routes, // short for `routes: routes`
});