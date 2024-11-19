<script setup lang="ts">
// Carbon
import "@carbon/web-components/es/components/breadcrumb/index";
import "@carbon/web-components/es/components/notification/index";
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/tabs/index";

// @ts-ignore
import Location16 from "@carbon/icons-vue/es/location/16";
// @ts-ignore
import User16 from "@carbon/icons-vue/es/user/16";
// @ts-ignore
import NetworkEnterprise16 from "@carbon/icons-vue/es/network--enterprise/16";
// @ts-ignore
import RecentlyViewed16 from "@carbon/icons-vue/es/recently-viewed/16";
import { toTitleCase } from "@/services/ForestClientService";
</script>

<template>
  <div id="screen" class="client-details-screen">
    <div class="client-details-content">
      <div class="resource-header">
        <cds-breadcrumb>
          <cds-breadcrumb-item>
            <cds-breadcrumb-link href="/search">Client search</cds-breadcrumb-link>
          </cds-breadcrumb-item>
        </cds-breadcrumb>

        <h1 class="resource-details--title">
          <span>
            <!-- TODO: Replace with real value -->
            {{ toTitleCase("Client Name") }}
          </span>
        </h1>
        <div>
          <p class="body-02 light-theme-text-text-secondary" data-testid="subtitle">
            Check and manage this client data
          </p>
        </div>
      </div>

      <div class="hide-when-less-than-two-children">
        <!--
          This div is necessary to avoid the div.header-offset below from interfering in the flex flow.
        -->
        <div data-scroll="top-notification" class="header-offset"></div>
      </div>

      <div class="grouping-14">
        <div class="grouping-05-short">
          <div>
            <h2 class="mg-tl-2 heading-06">Client summary</h2>

            <div class="grouping-10"></div>
          </div>
        </div>

        <cds-tabs value="locations" type="contained">
          <cds-tab id="tab-locations" target="panel-locations" value="locations">
            <div>
              Client locations
              <Location16 />
            </div>
          </cds-tab>
          <cds-tab id="tab-contacts" target="panel-contacts" value="contacts">
            <div>
              Client contacts
              <User16 />
            </div>
          </cds-tab>
          <cds-tab id="tab-related" target="panel-related" value="related">
            <div>
              Related clients
              <NetworkEnterprise16 />
            </div>
          </cds-tab>
          <cds-tab id="tab-activity" target="panel-activity" value="activity">
            <div>
              Activity log
              <RecentlyViewed16 />
            </div>
          </cds-tab>
        </cds-tabs>
      </div>
    </div>
    <div class="tab-panel">
      <div id="panel-locations" role="tabpanel" aria-labelledby="tab-locations" hidden></div>
      <div id="panel-contacts" role="tabpanel" aria-labelledby="tab-contacts" hidden></div>
      <div id="panel-related" role="tabpanel" aria-labelledby="tab-related" hidden></div>
      <div id="panel-activity" role="tabpanel" aria-labelledby="tab-activity" hidden></div>
    </div>
  </div>
</template>
