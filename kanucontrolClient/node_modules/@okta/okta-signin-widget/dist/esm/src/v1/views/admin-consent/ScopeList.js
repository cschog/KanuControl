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
import ScopeItem from './ScopeItem.js';

const SCOPE_N_GROUP_CONFIG = {
  groups: 'user',
  myAccount: 'user',
  users: 'user',
  apps: 'resource',
  authenticators: 'resource',
  authorizationServers: 'resource',
  clients: 'resource',
  domains: 'resource',
  factors: 'resource',
  idps: 'resource',
  linkedObjects: 'resource',
  policies: 'resource',
  templates: 'resource',
  eventHooks: 'hook',
  inlineHooks: 'hook',
  events: 'system',
  logs: 'system',
  orgs: 'system',
  roles: 'system',
  schemas: 'system',
  sessions: 'system',
  trustedOrigins: 'system'
};
const DEFAULT_GROUP = 'system';

const findScopeGroupKey = ({
  name = ''
}) => {
  const xs = name.split('.');
  const groupType = xs[1];
  return SCOPE_N_GROUP_CONFIG[groupType] || DEFAULT_GROUP;
};

const ScopeGroupHeaderView = View.extend({
  className: 'scope-group',
  events: {
    'click': 'expandScopes'
  },
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

      return "<div class=\"scope-group--header\"><h3>" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "groupName") || (depth0 != null ? lookupProperty(depth0, "groupName") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "groupName",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 37
          },
          "end": {
            "line": 1,
            "column": 50
          }
        }
      }) : helper)) + "</h3><span class=\"scope-group--toggle\"><svg class=\"caret\" width=\"12\" height=\"12\" viewBox=\"0 0 12 12\" fill=\"none\" xmlns=\"http://www.w3.org/2000/svg\"><path class=\"path\" fill-rule=\"evenodd\" clip-rule=\"evenodd\" d=\"M5.99967 4.66663L10.2663 8.66663L11.333 7.66663L5.99967 2.66663L0.666342 7.73636L1.73301 8.66663L5.99967 4.66663Z\" fill=\"#A0A7AC\"></path></svg></span></div>";
    },
    "useData": true
  }),
  expandScopes: function () {
    this.$el.toggleClass('scope-group--is-expanded');
    this.$('.caret').toggleClass('caret--is-rotated');
  },
  preRender: function () {
    oktaUnderscore.chain(this.options.scopes).sortBy(({
      name: name
    }) => name).each(({
      name: name,
      displayName: displayName,
      description: description
    }) => {
      this.add(ScopeItem, {
        options: {
          name: displayName || name,
          description: description
        }
      });
    });
  }
});
var AdminScopeList = View.extend({
  className: 'scope-list detail-row',
  postRender: function () {
    const allScopes = this.model.get('scopes');

    const scopesWithGroup = oktaUnderscore.groupBy(allScopes, findScopeGroupKey);

    const SCOPE_GROUP_NAMES_CONFIG = {
      'user': loc('admin.consent.group.user.group', 'login'),
      'resource': loc('admin.consent.group.resource.policy', 'login'),
      'hook': loc('admin.consent.group.hook', 'login'),
      'system': loc('admin.consent.group.system', 'login')
    }; // loop through SCOPE_GROUP_NAMES_CONFIG to keep group order consistent in UI.

    oktaUnderscore.each(SCOPE_GROUP_NAMES_CONFIG, (groupName, groupKey) => {
      const scopes = scopesWithGroup[groupKey];

      if (!Array.isArray(scopes)) {
        return;
      } // add scope group header and scopes


      this.add(ScopeGroupHeaderView, {
        options: {
          groupName: groupName,
          scopes: scopes
        }
      });
    });
  }
});

export { AdminScopeList as default };
//# sourceMappingURL=ScopeList.js.map
