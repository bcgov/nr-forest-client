<script setup lang="ts">
import { ref, computed, watch } from "vue";
// Carbon
import "@carbon/web-components/es/components/ui-shell/index";
import "@carbon/web-components/es/components/breadcrumb/index";
import "@carbon/web-components/es/components/tag/index";
import "@carbon/web-components/es/components/accordion/index";
import "@carbon/web-components/es/components/notification/index";
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/modal/index";
import "@carbon/web-components/es/components/tooltip/index";
// Composables
import { useFetchTo, usePost } from "@/composables/useFetch";
import { useRouter } from "vue-router";
import { isSmallScreen, isMediumScreen } from "@/composables/useScreenSize";
import { useEventBus } from "@vueuse/core";
// Types
import type {
  SubmissionDetails,
  CodeNameType,
  ModalNotification,
} from "@/dto/CommonTypesDto";
import { formatDistanceToNow, format } from "date-fns";
import { greenDomain } from "@/CoreConstants";
// Imported User session
import ForestClientUserSession from "@/helpers/ForestClientUserSession";
// @ts-ignore
import Approved16 from "@carbon/icons-vue/es/task--complete/32";
// @ts-ignore
import Review16 from "@carbon/icons-vue/es/data--view--alt/32";
// @ts-ignore
import Check16 from "@carbon/icons-vue/es/checkmark/16";
// @ts-ignore
import Error16 from "@carbon/icons-vue/es/error--outline/16";

const toastBus = useEventBus<ModalNotification>("toast-notification");

//Route related
const router = useRouter();
const id = ref(router.currentRoute.value.params.id);

// Base data
const data = ref<SubmissionDetails>({
  submissionId: 0,
  submissionType: "",
  submissionStatus: "",
  submittedTimestamp: new Date(0),
  updateTimestamp: new Date(0),
  approvedTimestamp: new Date(0),
  updateUser: "",
  business: {
    businessType: "",
    incorporationNumber: "",
    clientNumber: "",
    organizationName: "",
    clientType: "",
    goodStandingInd: "",
  },
  contact: [
    {
      index: 0,
      firstName: "",
      lastName: "",
      contactType: "",
      phoneNumber: "",
      emailAddress: "",
      locations: [],
      userId: "",
    },
  ],
  address: [],
  matchers: {
    goodStanding: "",
    corporationName: "",
    incorporationNumber: "",
    contact: "",
  },
});

// Modal related
const approveModal = ref(false);
const rejectModal = ref(false);
const rejectReasons = ref<CodeNameType[]>([
  {
    code: "goodstanding",
    name: "Client is not in good standing with BC Registries",
  },
  { code: "duplicated", name: "Client already exists" },
]);
const selectedRejectReasons = ref<CodeNameType[] | undefined>([]);
const rejectReasonMessage = ref("");

// Data loading
useFetchTo(`/api/clients/submissions/${id.value}`, data);

const showClientNumberField = computed(() => {
  if (selectedRejectReasons.value && selectedRejectReasons.value.length > 0) {
    return selectedRejectReasons.value.some(
      (reason: CodeNameType) => reason.code === "duplicated"
    );
  }
  return false;
});

const rejectYesDisabled = computed(() => {
  if (selectedRejectReasons.value && selectedRejectReasons.value.length > 0) {
    if (showClientNumberField.value) return !rejectReasonMessage.value;
    return !selectedRejectReasons.value.some(
      (reason: CodeNameType) => reason.code === "goodstanding"
    );
  }
  return true;
});

const rejectionReasonMessage = computed(() => {
  if (selectedRejectReasons.value && selectedRejectReasons.value.length > 0) {
    return selectedRejectReasons.value.map(
      (reason: CodeNameType) => reason.code
    );
  }
  return [];
});

// Submit the form changes to the backend
const submit = (approved: boolean) => {
  rejectModal.value = false;
  approveModal.value = false;
  const { response } = usePost(
    `/api/clients/submissions/${id.value}`,
    {
      approved,
      reasons: rejectionReasonMessage.value,
      message: rejectReasonMessage.value,
    },
    {
      headers: {
        "x-user-id": ForestClientUserSession.user?.userId ?? "",
        "x-user-email": ForestClientUserSession.user?.email ?? "",
        "x-user-name": `${ForestClientUserSession.user?.firstName} ${ForestClientUserSession.user?.lastName}`,
      },
    }
  );
  watch(response, (response) => {
    if (response.status) {
      console.log(response);
      router.push({ name: "internal" });
      const toastNotification: ModalNotification = {
        kind: "Success",
        active: true,
        handler: () => {},
        ...(approved
          ? {
              message: `New client number has been created for “${normalizeString(
                data.value.business.organizationName
              )}”`,
              toastTitle: "Submission approved",
            }
          : {
              message: `New client number has been rejected for “${normalizeString(
                data.value.business.organizationName
              )}”`,
              toastTitle: "Submission rejected",
            }),
      };
      toastBus.emit(toastNotification);
    }
  });
};

// Normalize the string to capitalize the first letter of each word
const normalizeString = (input: string): string => {
  const words = input.split(" ");
  const capitalizedWords = words.map((word) => {
    if (word.length > 0) {
      return word[0].toUpperCase() + word.slice(1).toLocaleLowerCase();
    } else {
      return "";
    }
  });
  return capitalizedWords.join(" ");
};
// Get the icon for the row
const iconForRow = (requestType: string) => {
  if (requestType === "Auto approved client") return Approved16;
  return Review16;
};
// Format the date to a friendly format
const friendlyDate = (date: Date): string => {
  if (!date) return "";
  return `${formatDistanceToNow(new Date(date))} ago`;
};
const formattedDate = (date: Date): string => {
  if (!date) return "";
  return format(new Date(date), "MMM dd, yyyy");
};
// Format the good standing parameter
const goodStanding = (goodStanding: string): string => {
  if (goodStanding)
    return goodStanding === "Y" ? "Good standing" : "Not in good standing";
  return "Unknown";
};

const tagColor = (status: string) => {
  switch (status) {
    case "New":
      return "blue";
    case "Approved":
      return "green";
    case "Rejected":
      return "red";
    default:
      return "purple";
  }
};

const matchingData = computed(() => {
  let results = [];
  if (data.value.matchers.corporationName) {
    results = [...results, ...data.value.matchers.corporationName.split(",")];
  }
  if (data.value.matchers.incorporationNumber) {
    results = [
      ...results,
      ...data.value.matchers.incorporationNumber.split(","),
    ];
  }
  if (data.value.matchers.contact) {
    results = [...results, ...data.value.matchers.contact.split(",")];
  }
  return results;
});
</script>

<template>
  
    <div id="screen" class="submission-content">
      <div class="submission-header">
        <cds-breadcrumb>
          <cds-breadcrumb-item>
            <cds-breadcrumb-link href="/submissions">Submissions</cds-breadcrumb-link>
          </cds-breadcrumb-item>
          
        </cds-breadcrumb>

        <h3 class="submission-details--title">
          <component
            data-testid="display-row-icon"
            :is="iconForRow(data.submissionType)"      
            :alt="data.submissionType"
          />
          <span>
            {{ data.submissionType }}: {{ normalizeString(data.business.organizationName) }}
          </span>
        </h3>
        <p class="body-01" data-testid="subtitle" v-if="data.submissionType === 'Auto approved client'">Check this new client data</p>
        <p class="body-01" data-testid="subtitle" v-else>Check and manage this submission for a new client number</p>
      </div>

      <cds-actionable-notification
        v-if="data.submissionType === 'Auto approved client'"
        v-shadow="true"
        low-contrast="true"
        hide-close-button="true"
        open="true"
        kind="info"
        title="This submission was automatically approved by the system"      
      >    
        <div v-if="data.business.businessType !== 'U'">
          No matching client records or BC Registries standing issues were found. Review the details in the read-only version below.
        </div>    
        <div v-if="data.business.businessType === 'U'">
          No matching client records were found. Review the details in the read-only version below.
        </div> 
      </cds-actionable-notification>

      <cds-inline-notification
        v-if="data.submissionType === 'Review new client' && data.submissionStatus === 'Approved'"
        v-shadow="true"
        low-contrast="true"
        hide-close-button="true"
        open="true"
        kind="info"
        title="Submission approved:"      
      >    
        <div>This new client submission has already been reviewed and approved.</div>    
      </cds-inline-notification>

      <cds-actionable-notification
        v-if="
          data.submissionType === 'Review new client' &&
          data.matchers.goodStanding === 'Value not found' &&
          data.submissionStatus !== 'Approved'
        "
        v-shadow="true"
        low-contrast="true"
        hide-close-button="true"
        open="true"
        kind="warning"
        title="Unknown standing"      
        >    
        <div>
          <p>Check this client's standing with
            <a href="https://www.bcregistry.gov.bc.ca/"
              target="_blank"
              rel="noopener noreferrer">BC Registries</a>
            to determine if this submission should be approved or rejected.
          </p>
        </div>
      </cds-actionable-notification>

      <cds-actionable-notification
          v-if="data.submissionType === 'Review new client' && matchingData.length > 0 && data.submissionStatus !== 'Approved'"
          v-shadow="true"
          low-contrast="true"
          hide-close-button="true"
          open="true"
          kind="warning"
          title="Possible matching records found"      
        >    
        <div>
          <p class="body-compact-01">
            {{ matchingData.length }} similar client record 
            <span v-if="matchingData.length === 1">was</span> 
            <span v-else>were</span> 
            found. 
            Review their information in the Client Management System to determine if this submission should be approved or rejected:
          </p>
          <ul class="bulleted-list-disc body-compact-01">
            <li 
              v-for="duplicatedClient in data.matchers.corporationName?.split(',')" 
              :key="duplicatedClient">
                Partial match on business name: 
                <a target="_blank" :href="`https://${greenDomain}/int/client/client02MaintenanceAction.do?bean.clientNumber=${duplicatedClient.trim()}`">{{duplicatedClient.trim()}}</a>
            </li>
            <li 
              v-for="duplicatedClient in data.matchers.incorporationNumber?.split(',')" 
              :key="duplicatedClient">
                Partial match on incorporation number - Client number: 
                <a target="_blank" :href="`https://${greenDomain}/int/client/client02MaintenanceAction.do?bean.clientNumber=${duplicatedClient.trim()}`">{{duplicatedClient.trim()}}</a>
            </li>
            <li 
              v-for="duplicatedClient in data.matchers.contact?.split(',')" 
              :key="duplicatedClient">
                Matching one or more contacts - Client number: 
                <a target="_blank" :href="`https://${greenDomain}/int/client/client02MaintenanceAction.do?bean.clientNumber=${duplicatedClient.trim()}`">{{duplicatedClient.trim()}}</a>
            </li>
          </ul>
        </div>    
      </cds-actionable-notification>

      <div class="grouping-14">
        <div class="grouping-05-short">
          <div>
            <h6 class="mg-tl-2">Client summary</h6>
            <div class="grouping-10">
              
              <read-only-component label="Name">
                <span class="body-compact-01">{{ normalizeString(data.business.organizationName) }}</span>
              </read-only-component>
              
              <read-only-component label="Client number" v-if="data.business.clientNumber">
                <span class="body-compact-01">{{ data.business.clientNumber }}</span>
              </read-only-component>

              <read-only-component label="Client type">
                <span class="body-compact-01">{{ data.business.clientTypeDesc }}</span>
              </read-only-component>

              <read-only-component label="Birthdate" v-if="data.business.clientType === 'I'">
                <span class="body-compact-01">{{ data.business.birthdate }}</span>
              </read-only-component>

              <read-only-component label="Incorporation number" v-if="data.business.incorporationNumber">
                <span class="body-compact-01">{{ data.business.incorporationNumber }}</span>
              </read-only-component>

              <read-only-component label="B.C. Registries standing" v-if="data.business.businessType === 'R'">
                <span class="body-compact-01">{{ goodStanding(data.business.goodStandingInd) }}</span>
              </read-only-component>

              <read-only-component label="Last updated">
                <span class="body-compact-01">{{ friendlyDate(data.updateTimestamp) }} | {{ data.updateUser }}</span>
              </read-only-component>

              <read-only-component label="Submission status">
                <cds-tag :type="tagColor(data.submissionStatus)" title=""><span>{{ data.submissionStatus }}</span></cds-tag>
              </read-only-component>

              <read-only-component label="Approved on" v-if="data.submissionStatus === 'Approved'">
                <span class="body-compact-01">{{ friendlyDate(data.approvedTimestamp) }}</span>
              </read-only-component>
            </div>
          </div>

          <cds-accordion>
            <cds-accordion-item open title="Submitter information" size="lg" class="grouping-05-internal">
              <div class="grouping-10-internal">
              <read-only-component label="First name">
                <span class="body-compact-01">{{ normalizeString(data.contact[0].firstName) }}</span>
              </read-only-component>
              
              <read-only-component label="Last name">
                <span class="body-compact-01">{{ data.contact[0].lastName }}</span>
              </read-only-component>

              <read-only-component label="Email">
                    <cds-tooltip>
                      <div class="sb-tooltip-trigger" aria-labelledby="content">
                        <a :href="'mailto:'+data.contact[0].emailAddress"><span class="body-compact-01-colorless">{{ data.contact[0].emailAddress }}</span></a>
                      </div>
                      <cds-tooltip-content id="content">
                        Click to send email
                      </cds-tooltip-content>
                    </cds-tooltip>
                  </read-only-component>

                  <read-only-component label="Phone number">
                    <cds-tooltip>
                      <div class="sb-tooltip-trigger" aria-labelledby="content">
                        <a :href="'tel:'+data.contact[0].phoneNumber"><span class="body-compact-01-colorless">{{ data.contact[0].phoneNumber }}</span></a>
                      </div>
                      <cds-tooltip-content id="content">
                        Click to call
                      </cds-tooltip-content>
                    </cds-tooltip>
                  </read-only-component>

              <read-only-component label="Submitted on">
                <span class="body-compact-01">{{ formattedDate(data.updateTimestamp) }}</span>
              </read-only-component>

            </div>
            </cds-accordion-item>
          </cds-accordion>

        </div>

        <cds-accordion>
          <cds-accordion-item open title="Client location" size="lg"  class="grouping-13">
            <div
            class="grouping-12"
            v-for="location in data.address"
            :key="location.name">
            
              <hr class="grouping-divider" v-if="location.index > 0"/>
              <p class="body-01">{{location.name}}</p>
              <div class="grouping-07">
                <read-only-component label="Country">
                    <span class="body-compact-01">{{ location.country }}</span>
                  </read-only-component>
                  
                  <read-only-component label="Street address">
                    <span class="body-compact-01">{{ location.streetAddress }}</span>
                  </read-only-component>
                  
                  <read-only-component label="City">
                    <span class="body-compact-01">{{ location.city }}</span>
                  </read-only-component>

                  <read-only-component label="Province or territory">
                    <span class="body-compact-01">{{ location.province }}</span>
                  </read-only-component>

                  <read-only-component label="Postal code">
                    <span class="body-compact-01">{{ location.postalCode }}</span>
                  </read-only-component>
              </div>
            </div>
          </cds-accordion-item>
        </cds-accordion>

        <cds-accordion>
          <cds-accordion-item open title="Client contacts" size="lg"  class="grouping-13">
            <div
            class="grouping-12"
            v-for="contact in data.contact"
            :key="contact.index">

              <hr class="grouping-divider" v-if="contact.index > 0"/>
              <p class="body-01">{{contact.firstName}} {{ contact.lastName }}</p>
              <div class="grouping-07">
                <read-only-component label="Associated location">
                    <span class="body-compact-01">{{ contact.locations.join(", ") }}</span>
                  </read-only-component>               
                  <read-only-component label="Primary role">
                    <span class="body-compact-01">{{ contact.contactType }}</span>
                  </read-only-component>
                  
                  <read-only-component label="Email">
                    <cds-tooltip>
                      <div class="sb-tooltip-trigger" aria-labelledby="content">
                        <a :href="'mailto:'+contact.emailAddress"><span class="body-compact-01-colorless">{{ contact.emailAddress }}</span></a>
                      </div>
                      <cds-tooltip-content id="content">
                        Click to send email
                      </cds-tooltip-content>
                    </cds-tooltip>
                  </read-only-component>

                  <read-only-component label="Phone number">
                    <cds-tooltip>
                      <div class="sb-tooltip-trigger" aria-labelledby="content">
                        <a :href="'tel:'+contact.phoneNumber"><span class="body-compact-01-colorless">{{ contact.phoneNumber }}</span></a>
                      </div>
                      <cds-tooltip-content id="content">
                        Click to call
                      </cds-tooltip-content>
                    </cds-tooltip>
                  </read-only-component>
              </div>
            </div>
          </cds-accordion-item>
        </cds-accordion>

        <div class="grouping-15" v-if="data.submissionType === 'Review new client' && (data.submissionStatus !== 'Approved' && data.submissionStatus !== 'Rejected')">
          <cds-button kind="primary" @click="approveModal = !approveModal">
            <span>Approve submission</span>
            <Check16 slot="icon" />
          </cds-button>
          <span class="spacer" v-if="!isSmallScreen && !isMediumScreen"></span>
          <cds-button kind="danger" @click="rejectModal = !rejectModal">
            <span>Reject submission</span>
            <Error16 slot="icon" />
          </cds-button>
        </div>
        
      </div>

      <cds-modal 
        id="approve-modal" 
        size="sm"
        :open="approveModal" 
        @cds-modal-closed="approveModal = !approveModal"
        >
        
        <cds-modal-header>
          <cds-modal-close-button></cds-modal-close-button>
          <cds-modal-heading>Approve submission</cds-modal-heading>
        </cds-modal-header>
        
        <cds-modal-body>
          <p class="body-compact-01">A new client number will be created and an email will be sent to the submitter.</p>
        </cds-modal-body>

        <cds-modal-footer>
          <cds-modal-footer-button 
            kind="secondary" 
            data-modal-close
            class="cds--modal-close-btn">
              Cancel
          </cds-modal-footer-button>
          <cds-modal-footer-button 
            kind="primary" 
            @click="submit(true)"
            class="cds--modal-submit-btn">
            <span>Approve submission</span>
            <Check16 slot="icon" />
          </cds-modal-footer-button>
        </cds-modal-footer>

      </cds-modal>

      <cds-modal 
        id="reject-modal"
        size="sm" 
        :open="rejectModal" 
        @cds-modal-closed="rejectModal = !rejectModal"
        >

        <cds-modal-header>
          <cds-modal-close-button></cds-modal-close-button>
          <cds-modal-heading>Reject submission</cds-modal-heading>
        </cds-modal-header>

        <cds-modal-body class="grouping-12">
          <p class="body-compact-01">This submission will be rejected and the submitter will receive an email notification. Please choose the reason below:</p>
          <multiselect-input-component
            id="reject_reason_id"
            label="Reason for rejection"
            tip="Choose one or more reasons"
            initial-value=""
            :model-value="rejectReasons"
            :selectedValues="[]"
            :validations="[]"
            @update:selected-value="event => selectedRejectReasons = event"        
          />
          <text-input-component
            :class="{invisible: !showClientNumberField}"
            id="reject_reason_message"
            label="Matching client number"
            placeholder=""
            v-model="rejectReasonMessage"
            :validations="[]"
            :enabled="true"
          />
        </cds-modal-body>

        <cds-modal-footer>
          
          <cds-modal-footer-button 
            kind="secondary" 
            data-modal-close
            class="cds--modal-close-btn">
            Cancel
          </cds-modal-footer-button>
          
          <cds-modal-footer-button 
            kind="danger"
            @click="submit(false)"
            :disabled="rejectYesDisabled"
            class="cds--modal-close-btn">
            <span>Reject submission</span>
            <Error16 slot="icon" />
          </cds-modal-footer-button>
        </cds-modal-footer>
      
      </cds-modal>

  </div>

</template>
