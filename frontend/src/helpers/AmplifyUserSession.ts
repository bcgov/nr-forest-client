import { Auth, Hub } from 'aws-amplify'
import type { AmplifyCustomProperties, Submitter } from '@/dto/CommonTypesDto'
import { cognitoEnv } from '@/CoreConstants'

class AmplifyUserSession implements AmplifyCustomProperties {
  public user: Submitter | undefined

  public constructor () {
    Hub.listen('auth', this.handleAuthEvent)
    this.loadUser()
  }

  logIn = async (provider: string): Promise<void> => {
    try {
      await Auth.federatedSignIn({
        customProvider: `${cognitoEnv ?? 'DEV'}-${provider.toUpperCase()}`
      })
      await this.loadDetails()
    } catch (e) {
      this.clearUser()
    }
  }

  logOut = async (): Promise<void> => {
    try {
      this.clearUser()
      await Auth.signOut()
    } catch (e) {}
  }

  isLoggedIn = async (): Promise<boolean> => {
    return (await this.loadDetails()) !== undefined
  }

  loadDetails = async (): Promise<Submitter | undefined> => {
    if (this.user === undefined) {
      this.loadUser()
    }

    if (this.user === undefined) {
      try {
        const response = await Auth.currentSession()
        if (response) {
          this.user = {
            name: response.getIdToken().payload['custom:idp_display_name'],
            provider: response.getIdToken().payload['custom:idp_name'],
            userId: response.getIdToken().payload['custom:idp_user_id'],
            email: response.getIdToken().payload.email,
            ...this.processName(
              response.getIdToken().payload,
              response.getIdToken().payload['custom:idp_name']
            )
          }
          sessionStorage.setItem('user', JSON.stringify(this.user))
        }
      } catch (e) {
        this.clearUser()
      }
    }
    return this.user
  }

  private processName = (
    payload: any,
    provider: string
  ): { firstName: string; lastName: string; businessName: string } => {
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
        additionalInfo.lastName = this.splitAtSpace(
          payload['custom:idp_display_name']
        )[0].replace(/,/g, '')
        additionalInfo.firstName = this.splitAtSpace(
          payload['custom:idp_display_name']
        )[1].replace(/,/g, '')
        additionalInfo.businessName = payload['custom:idp_business_name']
      }
    }

    return additionalInfo
  }

  private splitAtSpace = (name: string): string[] => {
    const nameArray = name.split(' ')
    const nameArrayWithoutSpaces = nameArray.filter((name) => name !== '')
    return nameArrayWithoutSpaces
  }

  private handleAuthEvent = async (data: any) => {
    if (data.payload.event === 'signIn' || data.payload.event === 'signOut') {
      await this.loadDetails()
    }
  }

  private clearUser = (): void => {
    this.user = undefined
    sessionStorage.removeItem('user')
  }

  private loadUser = (): void => {
    const user = sessionStorage.getItem('user')
    if (user) {
      this.user = JSON.parse(user)
    }
  }
}

export default new AmplifyUserSession()
