declare module './store/index' {
  import Vuex from 'vuex';

  export default class Store extends Vuex.Store<any> {
    readonly state: {
      jwt: string | null;
    };
  }
}
