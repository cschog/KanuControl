import FormController from '../util/FormController.js';

/* eslint-disable max-depth */
var RefreshAuthStateController = FormController.extend({
  className: 'refresh-auth-state',
  Model: {},
  Form: {
    noButtonBar: true
  },
  preRender: function () {
    const appState = this.options.appState;
    const token = this.options.token;
    this.model.startTransaction(function (authClient) {
      appState.trigger('loading', true);

      if (token) {
        return authClient.tx.introspect({
          stateToken: token
        });
      } // get stateToken from cookie
      // currently only applies to old pipeline


      if (authClient.tx.exists()) {
        return authClient.tx.resume();
      }

      appState.trigger('navigate', '');
    });
  },
  remove: function () {
    this.options.appState.trigger('loading', false);
    return FormController.prototype.remove.apply(this, arguments);
  }
});

export { RefreshAuthStateController as default };
//# sourceMappingURL=RefreshAuthStateController.js.map
