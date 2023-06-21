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
    path: '/form-submitted',
    name:'confirmation',
    component: FormSubmittedPage,
    props: true
  },
  { 
    path: '/submissions',
    name:'internal',
    component: ReviewApplicationPage,
    props: true
  },
];


export const router = createRouter({  
  history: createWebHashHistory(),
  routes,
});