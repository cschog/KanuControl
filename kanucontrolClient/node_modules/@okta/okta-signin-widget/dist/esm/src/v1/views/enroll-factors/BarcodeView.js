import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import fn$1 from '../../../util/FactorUtil.js';
import fn from '../../util/RouterUtil.js';

var BarcodeView = View.extend({
  className: 'scan-instructions clearfix',
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var helper,
          alias1 = depth0 != null ? depth0 : container.nullContext || {},
          alias2 = container.hooks.helperMissing,
          alias3 = "function",
          alias4 = container.escapeExpression,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<div class=\"scan-instructions-details-wrapper\"><div class=\"scan-instructions-details\"><p>" + alias4((helper = (helper = lookupProperty(helpers, "instructions") || (depth0 != null ? lookupProperty(depth0, "instructions") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "instructions",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 89
          },
          "end": {
            "line": 1,
            "column": 105
          }
        }
      }) : helper)) + "</p></div></div><div class=\"scan-instructions-qrcode-wrapper\"><div class=\"qrcode-wrap\"><img data-se=\"qrcode\" alt=\"" + alias4((helper = (helper = lookupProperty(helpers, "altQRCode") || (depth0 != null ? lookupProperty(depth0, "altQRCode") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "altQRCode",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 219
          },
          "end": {
            "line": 1,
            "column": 232
          }
        }
      }) : helper)) + "\" class=\"qrcode-image\" src=\"" + alias4((helper = (helper = lookupProperty(helpers, "qrcode") || (depth0 != null ? lookupProperty(depth0, "qrcode") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "qrcode",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 260
          },
          "end": {
            "line": 1,
            "column": 270
          }
        }
      }) : helper)) + "\"><div data-se=\"qrcode-success\" class=\"qrcode-success\"></div><div data-se=\"qrcode-error\" class=\"qrcode-error\"></div></div><a href=\"#\" data-type=\"manual-setup\" data-se=\"manual-setup\" class=\"link manual-setup\" aria-label=\"" + alias4((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.totp.aria.cannotScan"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 490
          },
          "end": {
            "line": 1,
            "column": 549
          }
        }
      })) + "\">" + alias4((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.totp.cannotScan"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 551
          },
          "end": {
            "line": 1,
            "column": 604
          }
        }
      })) + "</a><a href=\"#\" data-type=\"refresh-qrcode\" data-se=\"refresh-qrcode\" class=\"link refresh-qrcode\">" + alias4((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.totp.refreshBarcode"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 700
          },
          "end": {
            "line": 1,
            "column": 757
          }
        }
      })) + "</a></div>";
    },
    "useData": true
  }),
  events: {
    'click [data-type="manual-setup"]': function (e) {
      e.preventDefault();
      const url = fn.createActivateFactorUrl(this.model.get('__provider__'), this.model.get('__factorType__'), 'manual');

      if (this.model.get('__factorType__') === 'push') {
        // cancel the poll and navigate to manual setup.
        this.model.doTransaction(function (transaction) {
          return transaction.prev().then(function (trans) {
            const factor = oktaUnderscore.findWhere(trans.factors, {
              factorType: 'push',
              provider: 'OKTA'
            });

            return factor.enroll();
          });
        }).then(() => {
          this.options.appState.trigger('navigate', url);
        });
      } else {
        this.options.appState.trigger('navigate', url);
      }
    },
    'click [data-type="refresh-qrcode"]': function (e) {
      e.preventDefault();
      this.model.trigger('errors:clear');
      const self = this;
      this.model.doTransaction(function (transaction) {
        if (this.appState.get('isWaitingForActivation')) {
          return transaction.poll();
        } else {
          return transaction.activate();
        }
      }).then(function (trans) {
        const res = trans.data;

        if ((res.status === 'MFA_ENROLL_ACTIVATE' || res.status === 'FACTOR_ENROLL_ACTIVATE') && res.factorResult === 'WAITING') {
          // defer the render here to have a lastResponse set in AppState
          // so that we get new QRcode rendered
          oktaUnderscore.defer(oktaUnderscore.bind(self.render, self));
        }
      });
    }
  },
  initialize: function () {
    this.listenTo(this.options.appState, 'change:lastAuthResponse', function () {
      if (this.options.appState.get('isMfaEnrollActivate')) {
        this.$el.toggleClass('qrcode-expired', !this.options.appState.get('isWaitingForActivation'));
      } else if (this.options.appState.get('isSuccessResponse')) {
        this.$el.addClass('qrcode-success');
      }
    });
    this.listenTo(this.model, 'error', function () {
      if (this.options.appState.get('isMfaEnrollActivate')) {
        this.$el.toggleClass('qrcode-expired', true);
      }
    });
  },
  getTemplateData: function () {
    const factorName = fn$1.getFactorLabel(this.model.get('__provider__'), this.model.get('__factorType__'));
    let instructions;

    if (this.model.get('__provider__') === 'GOOGLE') {
      instructions = loc('enroll.totp.setupGoogleAuthApp', 'login', [factorName]);
    } else {
      instructions = loc('enroll.totp.setupApp', 'login', [factorName]);
    }

    return {
      instructions: instructions,
      qrcode: this.options.appState.get('qrcode'),
      altQRCode: loc('mfa.altQrCode', 'login')
    };
  }
});

export { BarcodeView as default };
//# sourceMappingURL=BarcodeView.js.map
