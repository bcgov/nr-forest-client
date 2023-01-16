export const newClientData = {
  begin: {
    reason: [""],
    tenure_type: [""],
    client_type: "",
  },
  information: {
    first_name: "",
    last_name: "",
    birthdate: "",
    registration_number: "",
    doing_business_as_check: false,
    doing_business_as: "",
    business_name: "",
    worksafebc_number: "",
  },
  contact: {
    address: [
      {
        stree_address: "",
        country: "",
        province: "",
        city: "",
        postal_code: "",
        index: 0, // any array data need to have this index, as an auto generated random number to be as unique identity
      },
    ],
  },
  authorized: {
    individuals: [
      {
        contact_type: "",
        firsname_or_company: "",
        last_name: "",
        phone: "",
        email: "",
        index: 0, // need use this index to be unique identity when display data in form tables
      },
    ],
  },
};
