import { Controller, Post, Body, Headers, UnauthorizedException } from '@nestjs/common';
import { WebhooksService } from './webhooks.service';

@Controller('webhooks')
export class WebhooksController {
  constructor(private readonly webhooksService: WebhooksService) {}

  @Post('dronahq')
  async handleDronaHQWebhook(
    @Headers('x-dronahq-signature') signature: string,
    @Body() payload: any
  ) {
    // In production: Verify signature using DRONAHQ_WEBHOOK_SECRET
    if (!signature) {
      // throw new UnauthorizedException('Missing signature');
    }

    return this.webhooksService.processConsultantAction(payload);
  }
}
