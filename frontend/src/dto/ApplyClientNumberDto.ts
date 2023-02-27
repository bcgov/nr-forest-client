export const formDataDto = {
  businessType: {
    clientType: null,
  },
  businessInformation: {
    firstName: "",
    lastName: "",
    birthdate: "",
    incorporationNumber: "",
    doingBusinessAsName: "",
    businessName: null,
  },
  location: {
    addresses: [
      {
        streetAddress: "",
        country: "",
        province: "",
        city: "",
        postalCode: "",
        businessPhone: "",
        email: "",
        index: 0, // any array data need to have this index, as an auto generated random number to be as unique identity
        contacts: [
          {
            contactType: "",
            name: "",
            businessPhone: "",
            email: "",
            index: 0, // need use this index to be unique identity when display data in form tables
          },
        ],
      },
    ],
  },
  submitterInformation: {
    firstName: "",
    lastName: "",
    phoneNumber: "",
    email: "",
  },
};
