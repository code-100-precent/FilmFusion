import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex);

// 定义状态类型
interface State {
  // 在这里添加你的状态
}

// 创建 store
const store = new Vuex.Store<State>({
  state: {
    // 在这里定义初始状态
  },
  mutations: {
    // 在这里定义 mutations
  },
  actions: {
    // 在这里定义 actions
  },
  modules: {
    // 在这里定义 modules
  }
});

export default store;
