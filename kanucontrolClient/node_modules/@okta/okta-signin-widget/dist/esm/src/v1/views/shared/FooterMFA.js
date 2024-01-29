import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import FooterSignout from './FooterSignout.js';

var FooterMFA = FooterSignout.extend({
  template: _Handlebars2.template({
    "1": function (container, depth0, helpers, partials, data) {
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

      return "<a href=\"" + alias4((helper = (helper = lookupProperty(helpers, "factorPageCustomLinkHref") || (depth0 != null ? lookupProperty(depth0, "factorPageCustomLinkHref") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "factorPageCustomLinkHref",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 40
          },
          "end": {
            "line": 1,
            "column": 68
          }
        }
      }) : helper)) + "\" data-se=\"factor-page-custom-link\" class=\"link js-factor-page-custom-link\" rel=\"noopener noreferrer\" target=\"_blank\">" + alias4((helper = (helper = lookupProperty(helpers, "factorPageCustomLinkText") || (depth0 != null ? lookupProperty(depth0, "factorPageCustomLinkText") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "factorPageCustomLinkText",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 186
          },
          "end": {
            "line": 1,
            "column": 214
          }
        }
      }) : helper)) + "</a>";
    },
    "3": function (container, depth0, helpers, partials, data) {
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

      return "<a href=\"#\" class=\"link " + alias4((helper = (helper = lookupProperty(helpers, "linkClassName") || (depth0 != null ? lookupProperty(depth0, "linkClassName") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "linkClassName",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 265
          },
          "end": {
            "line": 1,
            "column": 282
          }
        }
      }) : helper)) + "\" data-se=\"signout-link\">" + alias4((helper = (helper = lookupProperty(helpers, "linkText") || (depth0 != null ? lookupProperty(depth0, "linkText") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "linkText",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 307
          },
          "end": {
            "line": 1,
            "column": 319
          }
        }
      }) : helper)) + "</a>";
    },
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var stack1,
          alias1 = depth0 != null ? depth0 : container.nullContext || {},
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "hasFactorPageCustomLink") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(1, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 0
          },
          "end": {
            "line": 1,
            "column": 225
          }
        }
      })) != null ? stack1 : "") + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "showLink") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(3, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 225
          },
          "end": {
            "line": 1,
            "column": 330
          }
        }
      })) != null ? stack1 : "");
    },
    "useData": true
  }),
  className: 'auth-footer clearfix',
  getTemplateData: function () {
    const signoutTemplateData = FooterSignout.prototype.getTemplateData.apply(this, arguments);
    const factorPageCustomLinkHref = this.settings.get('helpLinks.factorPage.href');
    const factorPageCustomLinkText = this.settings.get('helpLinks.factorPage.text');
    const showLink = !this.settings.get('features.hideSignOutLinkInMFA') && !this.settings.get('features.mfaOnlyFlow');
    return Object.assign({}, signoutTemplateData, {
      hasFactorPageCustomLink: factorPageCustomLinkText && factorPageCustomLinkHref,
      factorPageCustomLinkHref: factorPageCustomLinkHref,
      factorPageCustomLinkText: factorPageCustomLinkText,
      showLink: showLink
    });
  }
});

export { FooterMFA as default };
//# sourceMappingURL=FooterMFA.js.map
