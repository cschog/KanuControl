import IdentifierView from './IdentifierView.js';

var RedirectIdPView = IdentifierView.extend({
  render: function () {
    IdentifierView.prototype.render.apply(this, arguments);
    this.$el.find('.sign-in-with-idp .separation-line').hide();
    this.$el.find('.button-primary').hide();
  }
});

export { RedirectIdPView as default };
//# sourceMappingURL=RedirectIdPView.js.map
