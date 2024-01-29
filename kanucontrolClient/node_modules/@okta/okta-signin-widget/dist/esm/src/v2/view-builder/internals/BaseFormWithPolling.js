import polling from '../views/shared/polling.js';
import BaseForm from './BaseForm.js';

var BaseFormWithPolling = BaseForm.extend(Object.assign({
  initialize: function () {
    BaseForm.prototype.initialize.apply(this, arguments);
    this.listenTo(this.options.appState, 'change:dynamicRefreshInterval', this.updateRefreshInterval);
  },
  updateRefreshInterval: function () {
    if (this.polling) {
      this.stopPolling();
      this.startPolling(this.options.appState.get('dynamicRefreshInterval'));
    }
  }
}, polling));

export { BaseFormWithPolling as default };
//# sourceMappingURL=BaseFormWithPolling.js.map
