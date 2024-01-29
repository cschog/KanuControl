import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import Factor from '../../models/Factor.js';
import Q from 'q';
import fn from '../../../util/FactorUtil.js';
import FactorsDropDown from '../mfa-verify/dropdown/FactorsDropDown.js';

var FactorBeacon = View.extend({
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var helper,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<div class=\"beacon-blank auth-beacon\"><div class=\"beacon-blank js-blank-beacon-border auth-beacon-border\"></div></div><div class=\"bg-helper auth-beacon auth-beacon-factor " + container.escapeExpression((helper = (helper = lookupProperty(helpers, "className") || (depth0 != null ? lookupProperty(depth0, "className") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "className",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 171
          },
          "end": {
            "line": 1,
            "column": 184
          }
        }
      }) : helper)) + "\" data-se=\"factor-beacon\"><div class=\"okta-sign-in-beacon-border auth-beacon-border\"></div></div><div data-type=\"factor-types-dropdown\" class=\"factors-dropdown-wrap\"></div>";
    },
    "useData": true
  }),
  events: {
    'click .auth-beacon-factor': function (e) {
      e.preventDefault();
      e.stopPropagation();
      const expanded = this.$('.dropdown .options').toggle().is(':visible');
      this.$('a.option-selected').attr('aria-expanded', expanded);

      if (expanded) {
        this.$('#okta-dropdown-options').find('li.factor-option:first a').focus();
      }
    }
  },
  initialize: function () {
    this.options.appState.set('beaconType', 'factor');
  },
  getTemplateData: function () {
    const factors = this.options.appState.get('factors');
    let factor;
    let className;

    if (factors) {
      factor = fn.findFactorInFactorsArray(factors, this.options.provider, this.options.factorType);
    } else {
      factor = new Factor.Model(this.options.appState.get('factor'));
    }

    className = factor.get('iconClassName');
    return {
      className: className || ''
    };
  },
  postRender: function () {
    if (this.options.animate) {
      this.$('.auth-beacon-factor').fadeIn(200);
    }

    const appState = this.options.appState;

    if (appState.get('hasMultipleFactorsAvailable')) {
      this.add(FactorsDropDown, '[data-type="factor-types-dropdown"]');
    }
  },
  fadeOut: function () {
    const deferred = Q.defer();
    this.$('.auth-beacon-factor').fadeOut(200, function () {
      deferred.resolve();
    });
    return deferred.promise;
  },
  equals: function (Beacon, options) {
    return Beacon && this instanceof Beacon && options.provider === this.options.provider && (options.factorType === this.options.factorType || fn.isOktaVerify(options.provider, options.factorType) && fn.isOktaVerify(this.options.provider, this.options.factorType));
  }
});

export { FactorBeacon as default };
//# sourceMappingURL=FactorBeacon.js.map
