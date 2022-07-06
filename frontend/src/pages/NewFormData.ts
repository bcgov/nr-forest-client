import type { CodeDescr } from "@/core/CodeDescrType";

export const formProperty = {
  contact: [
    {
      id: "licenseeName",
      label: "Licensee, agreement holder, or BCTS office",
      required: true,
    },
    {
      id: "submitterFirstName",
      label: "Submitter first name",
      note: "Optional",
    },
    {
      id: "submitterLastName",
      label: "Submitter last name",
      required: true,
    },
    {
      id: "submitterPhone",
      label: "Submitter phone number",
      required: true,
    },
    {
      id: "submitterEmail",
      label: "Submitter email",
      required: true,
    },
  ],
  fieldObsSelect: {
    id: "naturalResourceDistrictCode",
    label: "Natural resource district",
    required: true,
  },
  fieldObsInput: [
    {
      id: "forestFileId",
      label: "Forest file ID or timber sale licence",
      tooltip:
        "Enter the forest file ID (tenure) or timber sale licence (BCTS).",
      note: "Optional for BCTS",
    },
    {
      id: "cuttingPermit",
      label: "Cutting permit",
      note: "Optional for BCTS",
    },
  ],
  fieldObsBlock: [
    {
      id: "cutBlockId",
      type: "input",
      label: "Cut block ID",
      required: true,
    },
    {
      id: "totalBlockHa",
      type: "input",
      label: "Total cut block hectares",
      required: true,
    },
    {
      id: "haOrgMappedDefArea",
      type: "input",
      label: "Hectares originally mapped as priority deferral area",
      required: true,
      tooltip:
        "'Mapped' refers to polygons identified as priority deferral areas in the operational (vector) mapping version of Map 1 Priority Deferral Areas. Map 1 can be downloaded at https://catalogue.data.gov.bc.ca/dataset/5e257660-02ae-4f22-b861-4b2f53aefb4e/resource/47333f4e-1c84-4bb5-b3fe-6031fa78de20",
    },
    {
      id: "deferralCategoryCodes",
      type: "checkbox",
      //TODO: Make it dynamic, not hard-coded
      options: [
        { text: "Big Tree", code: "big_tree" },
        { text: "Ancient", code: "ancient" },
        { text: "Remnant", code: "remnant" },
      ],
      label: "Check all that apply",
      required: true,
    },
    {
      id: "haKeptOrgMapping",
      type: "input",
      label:
        "Hectares <text style='font-weight: bold'>added</text> to deferral area mapping",
      tooltip: "Adding areas to deferrals.",
      note: "Optional",
    },
    {
      id: "haAddedOrgMapping",
      type: "input",
      label:
        "Hectares <text style='font-weight: bold'>deleted</text> from deferral area mapping",
      tooltip: "Areas that were mapped as deferrals but do not meet criteria.",
      note: "Optional",
    },
    {
      id: "haDeletedOrgMapping",
      type: "input",
      label:
        "Hectares <text style='font-weight: bold'>unchanged</text> from deferral area mapping",
      tooltip: "Maintaining deferral or non-deferral status as mapped.",
      note: "Optional",
    },
  ],
  attach: {
    id: "attachments",
    label: "Upload your files",
    required: true,
  },
};

export const formData = {
  licenseeName: "",
  submitterFirstName: "",
  submitterLastName: "",
  submitterPhone: "",
  submitterEmail: "",
  naturalResourceDistrictCode: {},
  forestFileId: "",
  cuttingPermit: "",
  cutBlocks: [
    {
      cutBlockId: "",
      totalBlockHa: "",
      haOrgMappedDefArea: "",
      deferralCategoryCodes: [],
      haKeptOrgMapping: "",
      haAddedOrgMapping: "",
      haDeletedOrgMapping: "",
    },
  ],
  attachments: [],
};

export const nrdList = [] as CodeDescr[]; //[i.e.: { code: "a", text: "First option", emailAddress: "m@m.com", value: {myObj} },]

export const fieldObsBlockDefaultNew = {
  cutBlockId: "",
  totalBlockHa: "",
  haOrgMappedDefArea: "",
  deferralCategoryCodes: [],
  haKeptOrgMapping: "",
  haAddedOrgMapping: "",
  haDeletedOrgMapping: "",
};
