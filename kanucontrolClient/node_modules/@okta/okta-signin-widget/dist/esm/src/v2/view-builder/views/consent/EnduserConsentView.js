import AdminConsentView from './AdminConsentView.js';
import ConsentViewFooter from './EnduserConsentViewFooter.js';
import EnduserConsentViewHeader from './EnduserConsentViewHeader.js';
import EnduserConsentAgreementText from './EnduserConsentAgreementText.js';

var EnduserConsentView = AdminConsentView.extend({
  Header: EnduserConsentViewHeader,
  Footer: ConsentViewFooter,
  postRender: function () {
    const scopeList = this.$el.find('.scope-list');
    const consentAgreementText = new EnduserConsentAgreementText().render().el;
    scopeList.after(consentAgreementText);
  }
});

export { EnduserConsentView as default };
//# sourceMappingURL=EnduserConsentView.js.map
