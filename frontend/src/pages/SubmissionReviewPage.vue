<script setup lang="ts">
import { ref, computed, watch, reactive } from "vue";
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
import { useFocus } from "@/composables/useFocus";
import { useRouter } from "vue-router";
import { useEventBus } from "@vueuse/core";
// Types
import type {
  SubmissionDetails,
  CodeNameType,
  ModalNotification,
} from "@/dto/CommonTypesDto";
import { formatDistanceToNow, format } from "date-fns";
import {
  adminEmail,
  getObfuscatedEmailLink,
  convertFieldNameToSentence,
  toTitleCase,
  goodStanding,
} from "@/services/ForestClientService";

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
import { getValidations } from "@/helpers/validators/SubmissionReviewValidations";

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
    registrationNumber: "",
    clientNumber: "",
    organizationName: "",
    clientType: "",
    goodStandingInd: "",
    clientTypeDesc: "",
    birthdate: "",
    district: "",
    districtDesc: ""
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
    registrationNumber: "",
    contact: "",
    location: "",
  },
  rejectionReason: "",
  confirmedMatchUserId: ""
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
let networkErrorMsg = ref("");

// Data loading
const { error: fetchError } = useFetchTo(`/api/clients/submissions/${id.value}`, data);
watch([fetchError], () => {
  if (fetchError.value.message) {
    networkErrorMsg.value = fetchError.value.message;
  }
});

const showClientNumberField = computed(() => {
  if (selectedRejectReasons.value && selectedRejectReasons.value.length > 0) {
    return selectedRejectReasons.value.some(
      (reason: CodeNameType) => reason.code === "duplicated"
    );
  }
  return false;
});

const rejectYesDisabled = computed(() => {
  if (rejectValidation.reasons) {
    if (showClientNumberField.value) return !rejectValidation.message;
    return false;
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

const submitDisabled = ref(false);

const reviewConflictError = ref(false);

// Submit the form changes to the backend
const submit = (approved: boolean) => {
  if (submitDisabled.value) return;
  submitDisabled.value = true;

  rejectModal.value = false;
  approveModal.value = false;
  const { response, error, loading } = usePost(
    `/api/clients/submissions/${id.value}`,
    {
      approved,
      reasons: rejectionReasonMessage.value,
      message: rejectReasonMessage.value,
    },
    {}
  );

  const { setScrollPoint } = useFocus();

  watch([error], () => {
    if (error.value.response.status === 409) {
      reviewConflictError.value = true;
    } else if (error.value.message) {
      networkErrorMsg.value = error.value.message;
    }
    if (reviewConflictError.value || networkErrorMsg.value) {
      setScrollPoint("top-notification");
    }
  });

  watch(response, (response) => {
    if (response.status) {
      router.push({ name: "submissions-list" });
      const toastNotification: ModalNotification = {
        kind: "Success",
        active: true,
        handler: () => {},
        ...(approved
          ? {
              message: `New client number has been created for “${toTitleCase(
                data.value.business.organizationName
              )}”`,
              toastTitle: "Submission approved",
            }
          : {
              message: `New client number has been rejected for “${toTitleCase(
                data.value.business.organizationName
              )}”`,
              toastTitle: "Submission rejected",
            }),
      };
      toastBus.emit(toastNotification);
    }
  });

  watch([loading, reviewConflictError], () => {
    submitDisabled.value = loading.value || reviewConflictError.value;
  });
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
  const { corporationName, registrationNumber, contact, location } = data.value.matchers;
  let results = [];

  if (corporationName) {
    results.push(...corporationName.split(","));
  }
  if (registrationNumber) {
    results.push(...registrationNumber.split(","));
  }
  if (contact) {
    results.push(...contact.split(","));
  }
  if (location) {
    results.push(...location.split(","));
  }

  return results.filter(
    (value, index, self) => {
      return self.indexOf(value) === index;
    }
  );
});

const getListItemContent = ref((clientNumbers, label) => {
  return clientNumbers ? renderListItem(label, clientNumbers.trim()) : "";
});

const getUrl = (duplicatedClient: string, label: string) => {
  const clientNumber = duplicatedClient.trim();
  let hash = "";
  if (["location", "contact"].includes(label)) {
    hash = `#${label}s`;
  }
  return `/clients/details/${clientNumber}${hash}`;
};

const renderListItem = (label, clientNumbers) => {
  let finalLabel = "";
  if (label === 'contact' || label === 'location') {
    finalLabel = "Matching one or more " + label + "s";
  }
  else if (label === 'corporationName') {
    finalLabel = "Possible match with existing business name and/or ID";
  }
  else {
    finalLabel = "Partial match on " + convertFieldNameToSentence(label).toLowerCase() ;
  }

  finalLabel += " - Client number: ";

  const clients = [...new Set<string>(clientNumbers.split(","))];
  finalLabel += clients
                  .map(clientNumber =>
                    '<a target="_blank" href="' + getUrl(clientNumber, label) + '">' +
                    clientNumber +
                    "</a>")
                  .join(', ');
  
  return (
    finalLabel
  );
};

const userhasAuthority = [
  "CLIENT_VIEWER",
  "CLIENT_EDITOR",
  "CLIENT_ADMIN",
  "CLIENT_SUSPEND",
].some((authority) => ForestClientUserSession.authorities.includes(authority));
const canSubmit = !ForestClientUserSession.authorities.includes('CLIENT_EDITOR') 
                    && !ForestClientUserSession.authorities.includes('CLIENT_SUSPEND') 
                    && !ForestClientUserSession.authorities.includes('CLIENT_ADMIN');

if (canSubmit) {
  submitDisabled.value = true;
}

const rejectValidation = reactive<Record<string, boolean>>({
  reasons: false,
  message: false,
});

const cleanedRejectionReason = computed(() => {
  return data.value.rejectionReason 
    ? "Client " + data.value.rejectionReason.replace(/<div>&nbsp;<\/div>/g, '').replace(/<p>/g, ' ').replace(/<\/p>/g, '') 
    : '';
});

const isProcessing = computed(() => {
  const processingStatus = (
    !data.value.business.clientNumber
    && data.value.submissionType === 'Staff submitted data'  
  );

  if (processingStatus) {
    setTimeout(() => location.reload(), 10000);
  }

  return processingStatus;
});
</script>

<template>
  
    <div id="screen" class="submission-content">
      <div class="resource-header">
        <cds-breadcrumb>
          <cds-breadcrumb-item>
            <cds-breadcrumb-link href="/submissions">Submissions</cds-breadcrumb-link>
          </cds-breadcrumb-item>
          
        </cds-breadcrumb>

        <h1 class="resource-details--title" v-if="userhasAuthority">
          <span>
            {{ toTitleCase(data.business.organizationName) }}
          </span>
        </h1>
        <div v-if="userhasAuthority">
        <p class="body-02 light-theme-text-text-secondary" data-testid="subtitle" v-if="data.submissionType === 'Auto approved client'">Check this new client data</p>
        <p class="body-02 light-theme-text-text-secondary" data-testid="subtitle" v-else>Check and manage this submission for a new client number</p>
        </div>
      </div>
      
      <div class="hide-when-less-than-two-children"><!--
        This div is necessary to avoid the div.header-offset below from interfering in the flex flow.
      -->
        <div data-scroll="top-notification" class="header-offset"></div>
        <cds-actionable-notification
          v-if="isProcessing"
          low-contrast="true"
          hide-close-button="true"
          open="true"
          kind="warning"
          title="This submission is being processed"      
        >    
          <div>
            <p>
              It may take a few minutes. Once completed, the client number will display in the “Client summary” section below.
            </p>
          </div>
        </cds-actionable-notification>

        <cds-actionable-notification
          v-if="networkErrorMsg !== '' && userhasAuthority"
          v-shadow="true"
          low-contrast="true"
          hide-close-button="true"
          open="true"
          kind="error"
          title="Something went wrong:"      
        >    
          <div>
            We're working to fix a problem with our network. Please try approving or rejecting the submission later.
            If this error persists, please email <span v-dompurify-html="getObfuscatedEmailLink(adminEmail)"></span> for help.
          </div>
        </cds-actionable-notification>

        <cds-actionable-notification
          v-if="reviewConflictError && userhasAuthority"
          v-shadow="true"
          low-contrast="true"
          hide-close-button="true"
          open="true"
          kind="error"
          title="Submission already approved or rejected"
        >
          <div>
          Reload the page for updated information.
          </div>
        </cds-actionable-notification>
      </div>

      <cds-actionable-notification
        v-if="data.submissionType === 'Auto approved client' && userhasAuthority"
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
        v-if="data.submissionType === 'Review new client' && data.submissionStatus === 'Approved' && userhasAuthority"
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
          data.submissionStatus !== 'Approved' &&
          userhasAuthority
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
          v-if="
          data.submissionType === 'Review new client' && 
          matchingData.length > 0 && 
          data.submissionStatus !== 'Approved' &&
          userhasAuthority
          "
          v-shadow="true"
          low-contrast="true"
          hide-close-button="true"
          open="true"
          kind="warning"
          title="Possible matching records found"      
        >    
        <div>
          <p class="body-compact-01">
            {{ matchingData.length }} similar client
            <span v-if="matchingData.length === 1">record was</span> 
            <span v-else>records were</span> 
            found. 
            Review their information in the Forests Client Management System to determine if this submission should be approved or rejected:
          </p>
          <ul class="bulleted-list-disc body-compact-01">
            <!-- The content here is sanitized using vue-3-sanitize, and it is safe. -->
            <!-- eslint-disable-next-line vue/no-v-html -->
            <li v-for="(matcher, label) in data.matchers" :key="matcher" 
                v-dompurify-html="getListItemContent(matcher, label)">
            </li>
          </ul>
        </div>    
      </cds-actionable-notification>

      <cds-actionable-notification
          v-if="!userhasAuthority"
          v-shadow="true"
          low-contrast="true"
          hide-close-button="true"
          open="true"
          kind="error"
          title="You are not authorized to access this page"
        >
          <div>
          Please email <span v-dompurify-html="getObfuscatedEmailLink(adminEmail)"></span> for help
          </div>
        </cds-actionable-notification>

        <cds-actionable-notification
        v-if="canSubmit && userhasAuthority"
        v-shadow="true"
        low-contrast="true"
        hide-close-button="true"
        open="true"
        kind="warning"
        title="You are not authorized to modify client information"      
        >    
        <div>
          <p>To change your role, email Client Admin at 
            <span v-dompurify-html="getObfuscatedEmailLink(adminEmail)"></span>
          </p>
        </div>
      </cds-actionable-notification>

      <div class="grouping-14" v-if="userhasAuthority">
        <div class="grouping-05-short">
          <div>
            <h2 class="mg-tl-2 heading-06">Client summary</h2>

            <div class="grouping-10">
              <read-only-component label="Name">
                <span class="body-compact-01">{{ toTitleCase(data.business.organizationName) }}</span>
              </read-only-component>
              
              <read-only-component label="Client number" v-if="data.business.clientNumber">
                <span class="body-compact-01">
                  <a target="_blank" :href="`/clients/details/${data.business.clientNumber}`">
                    {{ data.business.clientNumber }}
                  </a>
                </span>
              </read-only-component>

              <read-only-component label="Client type">
                <span class="body-compact-01">{{ data.business.clientTypeDesc }}</span>
              </read-only-component>

              <read-only-component
                label="Birthdate"
                v-if="data.business.clientType === 'I' || data.business.clientType === 'RSP'"
              >
                <span class="body-compact-01">{{ data.business.birthdate }}</span>
              </read-only-component>

              <read-only-component label="Registration number" v-if="data.business.registrationNumber">
                <span class="body-compact-01">{{ data.business.registrationNumber }}</span>
              </read-only-component>

              <read-only-component label="B.C. Registries standing" v-if="data.business.businessType === 'R'">
                <span class="body-compact-01">{{ goodStanding(data.business.goodStandingInd) }}</span>
              </read-only-component>

              <read-only-component label="District">
                <span class="body-compact-01">{{ data.business.districtDesc }}</span>
              </read-only-component>

              <read-only-component label="Submitted on">
                <span class="body-compact-01">{{ formattedDate(data.submittedTimestamp) }}</span>
              </read-only-component>

              <read-only-component label="Submission status">
                <cds-tag :type="tagColor(data.submissionStatus)" title=""><span>{{ data.submissionStatus }}</span></cds-tag>
              </read-only-component>

              <read-only-component label="Approved on" v-if="data.submissionStatus === 'Approved'">
                <span class="body-compact-01">{{ friendlyDate(data.approvedTimestamp) }}</span>
              </read-only-component>

              <read-only-component label="Rejected by" v-if="data.submissionStatus === 'Rejected'">
                <span class="body-compact-01">{{ data.confirmedMatchUserId }}</span>
              </read-only-component>

              <read-only-component label="Reason for rejection" v-if="data.submissionStatus === 'Rejected'">
                <span class="body-compact-01" 
                      style="width: 40rem" 
                      v-dompurify-html="cleanedRejectionReason">
                </span>
              </read-only-component>

            </div>
          </div>

          <cds-accordion>
            <cds-accordion-item open title="Submitter information" size="lg" class="grouping-05-internal">
              <div class="grouping-10-internal">
              <read-only-component label="First name">
                <span class="body-compact-01">{{ toTitleCase(data.contact[0].firstName) }}</span>
              </read-only-component>
              
              <read-only-component label="Last name">
                <span class="body-compact-01">{{ data.contact[0].lastName }}</span>
              </read-only-component>

              <read-only-component label="Email">
                    <cds-tooltip>
                      <div class="sb-tooltip-trigger" aria-labelledby="content">
                        <a :href="'mailto:'+data.contact[0].emailAddress"><span class="body-compact-01 colorless">{{ data.contact[0].emailAddress }}</span></a>
                      </div>
                      <cds-tooltip-content id="content">
                        Click to send email
                      </cds-tooltip-content>
                    </cds-tooltip>
                  </read-only-component>

                  <read-only-component label="Phone number">
                    <cds-tooltip>
                      <div class="sb-tooltip-trigger" aria-labelledby="content">
                        <a :href="'tel:'+data.contact[0].phoneNumber"><span class="body-compact-01 colorless">{{ data.contact[0].phoneNumber }}</span></a>
                      </div>
                      <cds-tooltip-content id="content">
                        Click to call
                      </cds-tooltip-content>
                    </cds-tooltip>
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
              <p class="body-01 light-theme-text-text-primary">{{location.name}}</p>
              <div class="grouping-07">                 
                <read-only-component label="Street address">
                  <span class="body-compact-01">{{ location.streetAddress }}</span>
                </read-only-component>
                  
                <read-only-component label="City">
                  <span class="body-compact-01">{{ toTitleCase(location.city) }}</span>
                </read-only-component>

                <read-only-component label="Province or territory">
                  <span class="body-compact-01">{{ location.province }}</span>
                </read-only-component>

                <read-only-component label="Country">
                  <span class="body-compact-01">{{ location.country }}</span>
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
              <p class="body-01 light-theme-text-text-primary">{{contact.firstName}} {{ contact.lastName }}</p>
              <div class="grouping-07">
                <read-only-component label="Associated location" v-if="$features.BCEID_MULTI_ADDRESS">
                  <span class="body-compact-01">{{ contact.locations.join(", ") }}</span>
                </read-only-component>               
                <read-only-component label="Primary role">
                  <span class="body-compact-01">{{ contact.contactType }}</span>
                </read-only-component>
                
                <read-only-component label="Email">
                  <cds-tooltip>
                    <div class="sb-tooltip-trigger" aria-labelledby="content">
                      <a :href="'mailto:'+contact.emailAddress"><span class="body-compact-01 colorless">{{ contact.emailAddress }}</span></a>
                    </div>
                    <cds-tooltip-content id="content">
                      Click to send email
                    </cds-tooltip-content>
                  </cds-tooltip>
                </read-only-component>

                <read-only-component label="Phone number">
                  <cds-tooltip>
                    <div class="sb-tooltip-trigger" aria-labelledby="content">
                      <a :href="'tel:'+contact.phoneNumber"><span class="body-compact-01 colorless">{{ contact.phoneNumber }}</span></a>
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
          <cds-button kind="danger--tertiary" 
                      @click="rejectModal = !rejectModal" 
                      :disabled="submitDisabled">
            <span>Reject submission</span>
            <Error16 slot="icon" />
          </cds-button>
          <span class="spacer" v-if="!isSmallScreen && !isMediumScreen"></span>
          <cds-button kind="primary" 
                      @click="approveModal = !approveModal" 
                      :disabled="submitDisabled">
            <span>Approve submission</span>
            <Check16 slot="icon" />
          </cds-button>
        </div>
        
      </div>

      <cds-modal 
        id="approve-modal"
        aria-labelledby="approve-modal-heading"
        aria-describedby="approve-modal-body"
        size="sm"
        :open="approveModal" 
        @cds-modal-closed="approveModal = !approveModal"
        >
        
        <cds-modal-header>
          <cds-modal-close-button></cds-modal-close-button>
          <cds-modal-heading id="approve-modal-heading">Approve submission</cds-modal-heading>
        </cds-modal-header>
        
        <cds-modal-body id="approve-modal-body">
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
            :disabled="submitDisabled"
            class="cds--modal-submit-btn">
            <span>Approve submission</span>
            <Check16 slot="icon" />
          </cds-modal-footer-button>
        </cds-modal-footer>

      </cds-modal>

      <cds-modal
        id="reject-modal"
        aria-labelledby="reject-modal-heading"
        aria-describedby="reject-modal-body"
        size="sm" 
        :open="rejectModal" 
        @cds-modal-closed="rejectModal = !rejectModal"
        >

        <cds-modal-header>
          <cds-modal-close-button></cds-modal-close-button>
          <cds-modal-heading id="reject-modal-heading">Reject submission</cds-modal-heading>
        </cds-modal-header>

        <cds-modal-body id="reject-modal-body" class="grouping-12">
          <p class="body-compact-01">This submission will be rejected and the submitter will receive an email notification. Please choose the reason below:</p>
          <multiselect-input-component
            id="reject_reason_id"
            label="Reason for rejection"
            required
            tip="Choose one or more reasons"
            initial-value=""
            :model-value="rejectReasons"
            :selectedValues="[]"
            :validations="[...getValidations('reasons')]"
            @update:selected-value="event => selectedRejectReasons = event"
            @empty="rejectValidation.reasons = !$event"
            @error="rejectValidation.reasons = !$event"
          />
          <text-input-component
            :class="{invisible: !showClientNumberField}"
            id="reject_reason_message"
            label="Matching client number"
            placeholder=""
            v-model="rejectReasonMessage"
            numeric
            :validations="[...getValidations('message')]"
            :required="showClientNumberField"
            :enabled="true"
            @empty="rejectValidation.message = !$event"
            @error="rejectValidation.message = !$event"
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
            :disabled="rejectYesDisabled || submitDisabled"
            class="cds--modal-close-btn">
            <span>Reject submission</span>
            <Error16 slot="icon" />
          </cds-modal-footer-button>
        </cds-modal-footer>
      
      </cds-modal>

  </div>

</template>
