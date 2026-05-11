import { describe, it, expect, vi, beforeEach } from "vitest";
import { mount } from "@vue/test-utils";
import { ref } from "vue";
import BcRegistryView from "@/pages/client-details/BcRegistryView.vue";
import type { BcRegistryInformation } from "@/dto/CommonTypesDto";

vi.mock("@/composables/useFetch", () => ({
  useFetchTo: vi.fn(),
}));

import { useFetchTo } from "@/composables/useFetch";

const mockFetchTo =
  (mockData: BcRegistryInformation[] | null, isLoading = false, mockError: any = null) =>
  (_url: any, received: any) => {
    if (mockData !== null) {
      received.value = mockData;
    }
    return {
      loading: ref(isLoading),
      error: ref(mockError),
      data: received,
      response: ref({}),
      fetch: vi.fn(),
    };
  };

const sampleBcRegistryData: BcRegistryInformation[] = [
  {
    business: {
      legalName: "Acme Corp",
      state: "ACTIVE",
      legalType: "BC",
      identifier: "BC1234567",
      registrationDateTime: "2020-03-15T00:00:00+00:00",
    },
    offices: {
      businessOffice: {
        mailingAddress: {
          streetAddress: "123 Main St",
          addressCity: "Victoria",
          addressRegion: "BC",
          postalCode: "V8V 1A1",
          addressCountry: "CA",
        },
        deliveryAddress: {
          streetAddress: "456 Oak Ave",
          addressCity: "Victoria",
          addressRegion: "BC",
          postalCode: "V8V 2B2",
          addressCountry: "CA",
        },
      },
    },
    parties: [],
  },
];

const sampleDataWithParties: BcRegistryInformation[] = [
  {
    ...sampleBcRegistryData[0],
    parties: [
      {
        officer: {
          id: "P001",
          firstName: "Jane",
          middleInitial: "A",
          lastName: "Smith",
        },
        mailingAddress: {
          streetAddress: "789 Pine Rd",
          addressCity: "Vancouver",
          addressRegion: "BC",
          postalCode: "V6B 3C3",
        },
        roles: [{ roleType: "Partner" }],
      },
      {
        officer: {
          id: "P002",
          organizationName: "Partner Co Ltd",
        },
        mailingAddress: {
          streetAddress: "321 Elm St",
          addressCity: "Kelowna",
          addressRegion: "BC",
          postalCode: "V1Y 4D4",
        },
        roles: [{ roleType: "Director" }],
      },
    ],
  },
];

describe("BcRegistryView.vue", () => {
  const createComponent = (registrationNumber = "BC1234567") =>
    mount(BcRegistryView, {
      props: { registrationNumber },
    });

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("loading state", () => {
    it("renders skeleton text while loading", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo(null, true),
      );

      const wrapper = createComponent();

      expect(wrapper.find(".skeleton-group").exists()).toBe(true);
      expect(wrapper.findAll("cds-skeleton-text").length).toBeGreaterThan(0);
    });

    it("does not render the business information section while loading", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo(null, true),
      );

      const wrapper = createComponent();

      expect(wrapper.find("#bc-business-information").exists()).toBe(false);
    });
  });

  describe("error state", () => {
    it("shows unavailable message when error has a message", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo(null, false, { message: "Not found" }),
      );

      const wrapper = createComponent();

      expect(wrapper.text()).toContain("BC Registry information not available.");
    });

    it("does not render the business information accordion on error", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo(null, false, { message: "Server error" }),
      );

      const wrapper = createComponent();

      expect(wrapper.find("#bc-business-information").exists()).toBe(false);
    });
  });

  describe("no data state", () => {
    it("shows not available message when data is null", () => {
      (useFetchTo as any).mockImplementation(mockFetchTo(null));

      const wrapper = createComponent();

      expect(wrapper.text()).toContain("BC Registry information not available.");
    });

    it("shows not available message when data is an empty array", () => {
      (useFetchTo as any).mockImplementation(mockFetchTo([]));

      const wrapper = createComponent();

      // Component treats empty array as no data and shows not available message
      expect(wrapper.text()).toContain("BC Registry information not available.");
      expect(wrapper.find("#bc-business-information").exists()).toBe(false);
    });
  });

  describe("populated state", () => {
    beforeEach(() => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo(sampleBcRegistryData),
      );
    });

    it("renders the BC Registry information heading", () => {
      const wrapper = createComponent();

      expect(wrapper.text()).toContain("BC Registry information");
    });

    it("renders the business information accordion", () => {
      const wrapper = createComponent();

      expect(wrapper.find("#bc-business-information").exists()).toBe(true);
    });

    it("renders the business name", () => {
      const wrapper = createComponent();

      expect(wrapper.find("#businessNameId").text()).toContain("Acme Corp");
    });

    it("renders the business status in title case", () => {
      const wrapper = createComponent();

      expect(wrapper.find("#businessStatusId").text()).toContain("Active");
    });

    it("renders the business type from legal type code", () => {
      const wrapper = createComponent();

      expect(wrapper.find("#businessTypeId").text()).toContain("BC Company");
    });

    it("renders the registration number", () => {
      const wrapper = createComponent();

      expect(wrapper.find("#registrationNumberId").text()).toContain("BC1234567");
    });

    it("renders the mailing address street", () => {
      const wrapper = createComponent();

      expect(wrapper.find("#officeStreetAddressId").text()).toContain("123 Main St");
    });

    it("renders the mailing address city/region/postal", () => {
      const wrapper = createComponent();

      const cityEl = wrapper.find("#officeCityId");
      expect(cityEl.text()).toContain("Victoria");
      expect(cityEl.text()).toContain("BC");
      expect(cityEl.text()).toContain("V8V 1A1");
    });

    it("renders the mailing address country", () => {
      const wrapper = createComponent();

      expect(wrapper.find("#officeCountryId").text()).toContain("CA");
    });

    it("renders the delivery address street", () => {
      const wrapper = createComponent();

      expect(wrapper.find("#deliveryStreetAddressId").text()).toContain("456 Oak Ave");
    });

    it("does not render the partner information accordion when parties is empty", () => {
      const wrapper = createComponent();

      expect(wrapper.find("#bc-partner-information").exists()).toBe(false);
    });
  });

  describe("with parties", () => {
    beforeEach(() => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo(sampleDataWithParties),
      );
    });

    it("renders the partner information accordion", () => {
      const wrapper = createComponent();

      expect(wrapper.find("#bc-partner-information").exists()).toBe(true);
    });

    it("renders the partners table", () => {
      const wrapper = createComponent();

      expect(wrapper.find("#partners-table").exists()).toBe(true);
    });

    it("renders individual party name from first/middle/last", () => {
      const wrapper = createComponent();

      expect(wrapper.find("#partners-table").text()).toContain("Jane A Smith");
    });

    it("renders organization party name", () => {
      const wrapper = createComponent();

      expect(wrapper.find("#partners-table").text()).toContain("Partner Co Ltd");
    });

    it("renders party role types", () => {
      const wrapper = createComponent();

      const tableText = wrapper.find("#partners-table").text();
      expect(tableText).toContain("Partner");
      expect(tableText).toContain("Director");
    });

    it("renders party IDs", () => {
      const wrapper = createComponent();

      const tableText = wrapper.find("#partners-table").text();
      expect(tableText).toContain("P001");
      expect(tableText).toContain("P002");
    });

    it("renders party mailing addresses", () => {
      const wrapper = createComponent();

      const tableText = wrapper.find("#partners-table").text();
      expect(tableText).toContain("789 Pine Rd");
      expect(tableText).toContain("321 Elm St");
    });
  });

  describe("fallback values", () => {
    it("shows '—' for missing business name", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo([{ business: {} }]),
      );

      const wrapper = createComponent();

      expect(wrapper.find("#businessNameId").text()).toContain("—");
    });

    it("uses resolvedLegalName when legalName is absent", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo([{ business: { resolvedLegalName: "Resolved Corp" } }]),
      );

      const wrapper = createComponent();

      expect(wrapper.find("#businessNameId").text()).toContain("Resolved Corp");
    });

    it("shows '—' for missing registration number", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo([{ business: {} }]),
      );

      const wrapper = createComponent();

      expect(wrapper.find("#registrationNumberId").text()).toContain("—");
    });

    it("shows '—' for missing registration date", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo([{ business: {} }]),
      );

      const wrapper = createComponent();

      expect(wrapper.find("#registrationDateId").text()).toContain("—");
    });

    it("shows '—' for missing mailing address street", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo([{ business: {} }]),
      );

      const wrapper = createComponent();

      expect(wrapper.find("#officeStreetAddressId").text()).toContain("—");
    });

    it("shows '—' for missing delivery address street", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo([{ business: {} }]),
      );

      const wrapper = createComponent();

      expect(wrapper.find("#deliveryStreetAddressId").text()).toContain("—");
    });

    it("renders delivery address city, region and postal code", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo(sampleBcRegistryData),
      );

      const wrapper = createComponent();

      const cityEl = wrapper.find("#deliveryCityId");
      expect(cityEl.text()).toContain("Victoria");
      expect(cityEl.text()).toContain("BC");
      expect(cityEl.text()).toContain("V8V 2B2");
    });
  });

  describe("officerName helper", () => {
    it("shows '—' when party has no officer", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo([{
          ...sampleBcRegistryData[0],
          parties: [
            {
              mailingAddress: { streetAddress: "1 Any St", addressCity: "Victoria", addressRegion: "BC", postalCode: "V8V 1A1" },
              roles: [{ roleType: "Director" }],
            },
          ],
        }]),
      );

      const wrapper = createComponent();

      // Name column has no officer → falls back to '—'
      expect(wrapper.find("#partners-table").text()).toContain("—");
    });

    it("shows '—' when officer has no name parts and no organization name", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo([{
          ...sampleBcRegistryData[0],
          parties: [
            {
              officer: { id: "P001" },
              mailingAddress: { streetAddress: "1 Any St", addressCity: "Victoria", addressRegion: "BC", postalCode: "V8V 1A1" },
              roles: [{ roleType: "Director" }],
            },
          ],
        }]),
      );

      const wrapper = createComponent();

      // All name parts undefined → join produces '' → || '—'
      expect(wrapper.find("#partners-table").text()).toContain("—");
    });

    it("renders individual name without middle initial", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo([{
          ...sampleBcRegistryData[0],
          parties: [
            {
              officer: { id: "P001", firstName: "John", lastName: "Doe" },
              mailingAddress: { streetAddress: "1 Any St", addressCity: "Victoria", addressRegion: "BC", postalCode: "V8V 1A1" },
              roles: [{ roleType: "Director" }],
            },
          ],
        }]),
      );

      const wrapper = createComponent();

      expect(wrapper.find("#partners-table").text()).toContain("John Doe");
    });
  });

  describe("partyAddress helper", () => {
    it("shows '—' when party has no mailing address", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo([{
          ...sampleBcRegistryData[0],
          parties: [
            {
              officer: { id: "P001", firstName: "Bob" },
              roles: [{ roleType: "Director" }],
            },
          ],
        }]),
      );

      const wrapper = createComponent();

      // Address column has no mailingAddress → '—'
      expect(wrapper.find("#partners-table").text()).toContain("—");
    });

    it("shows '—' when all address fields are empty", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo([{
          ...sampleBcRegistryData[0],
          parties: [
            {
              officer: { id: "P001", firstName: "Bob" },
              mailingAddress: { streetAddress: "", addressCity: "", addressRegion: "", postalCode: "" },
              roles: [{ roleType: "Director" }],
            },
          ],
        }]),
      );

      const wrapper = createComponent();

      // street + line2 are both empty → [].filter(Boolean).join(' ') is '' → || '—'
      expect(wrapper.find("#partners-table").text()).toContain("—");
    });

    it("renders street-only address when city/region/postal are absent", () => {
      (useFetchTo as any).mockImplementation(
        mockFetchTo([{
          ...sampleBcRegistryData[0],
          parties: [
            {
              officer: { id: "P001", firstName: "Bob" },
              mailingAddress: { streetAddress: "99 Oak Lane" },
              roles: [{ roleType: "Director" }],
            },
          ],
        }]),
      );

      const wrapper = createComponent();

      expect(wrapper.find("#partners-table").text()).toContain("99 Oak Lane");
    });
  });
});
