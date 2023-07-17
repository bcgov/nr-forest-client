import { Auth } from 'aws-amplify'
import type { AmplifyCustomProperties } from '@/dto/CommonTypesDto'
import { cognitoEnv } from '@/CoreConstants'

const splitAtSpace = (name: string) => {
  const nameArray = name.split(' ')
  const nameArrayWithoutSpaces = nameArray.filter((name) => name !== '')
  return nameArrayWithoutSpaces
}

const processName = (payload: any, provider: string) => {
  const additionalInfo = { firstName: '', lastName: '', businessName: '' }
  if (payload.given_name) {
    additionalInfo.firstName = payload.given_name
  }

  if (payload.family_name) {
    additionalInfo.lastName = payload.family_name
  }

  if (
    provider === 'bceidbusiness' ||
    (additionalInfo.firstName === '' && additionalInfo.lastName === '')
  ) {
    const name = payload['custom:idp_display_name']
    const spaceIndex = name.indexOf(' ')
    if (spaceIndex > 0) {
      additionalInfo.lastName = splitAtSpace(
        payload['custom:idp_display_name']
      )[0].replace(/,/g, '')
      additionalInfo.firstName = splitAtSpace(
        payload['custom:idp_display_name']
      )[1].replace(/,/g, '')
      additionalInfo.businessName = payload['custom:idp_business_name']
    }
  }

  return additionalInfo
}

const amplifyUserSession: AmplifyCustomProperties = {
  user: undefined,
  async logIn(provider: string) {
    try {
      await Auth.federatedSignIn({
        customProvider: `${cognitoEnv ?? 'DEV'}-${provider.toUpperCase()}`
      })
    } catch (e) {
      this.user = undefined
    }
  },
  async logOut() {
    try {
      await Auth.signOut()
      this.user = undefined
    } catch (e) {}
  },
  async loadDetails() {
    if (this.user === undefined) {
      try {
        const response = await Auth.currentSession()
        if (response) {
          this.user = {
            name: response.getIdToken().payload['custom:idp_display_name'],
            provider: response.getIdToken().payload['custom:idp_name'],
            userId: response.getIdToken().payload['custom:idp_user_id'],
            email: response.getIdToken().payload.email,
            ...processName(
              response.getIdToken().payload,
              response.getIdToken().payload['custom:idp_name']
            )
          }
        }
      } catch (e) {
        this.user = undefined
      }
    }
    return this.user
  },
  async isLoggedIn() {
    await this.loadDetails()
    return this.user !== undefined
  }
}

export default amplifyUserSession
